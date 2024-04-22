package database

import (
	"context"
	"crypto/tls"
	"crypto/x509"
	"database/sql"
	"encoding/pem"
	"errors"
	"fmt"
	"github.com/alexlast/bunzap"
	"github.com/jackc/pgx/v4"
	"github.com/jackc/pgx/v4/stdlib"
	"github.com/uptrace/bun"
	"github.com/uptrace/bun/dialect"
	"github.com/uptrace/bun/dialect/pgdialect"
	"github.com/uptrace/bun/dialect/sqlitedialect"
	"github.com/uptrace/bun/extra/bunotel"
	"github.com/uptrace/bun/migrate"
	"go.uber.org/zap"
	"modernc.org/sqlite"
	"os"
	"runtime"
	"strings"
	"tyr.codes/tyr/LongshipLink/auth/database/migrations"
)

const (
	dbTypePostgres = "postgres"
	dbTypeSqlite   = "sqlite"

	dbTLSModeDisable = "disable"
	dbTLSModeEnable  = "enable"
	dbTLSModeRequire = "require"
	dbTLSModeUnset   = ""
)

// Client is a DB interface compatible client for Bun.
type Client struct {
	db      *bun.DB
	errProc func(error) Error
}

// New creates a new bun database client
func New(ctx context.Context) (*Client, error) {
	var newBun *Client
	var err error
	dbType := dbTypePostgres

	switch dbType {
	case dbTypePostgres:
		newBun, err = pgConn(ctx)
		if err != nil {
			return nil, err
		}
	case dbTypeSqlite:
		newBun, err = sqliteConn(ctx)
		if err != nil {
			return nil, err
		}
	default:
		return nil, fmt.Errorf("database type %s not supported for bundb", dbType)
	}

	newBun.db.AddQueryHook(bunotel.NewQueryHook(bunotel.WithDBName(os.Getenv("DB_DATABASE"))))

	// Add a query hook to log all queries (debug)
	newBun.db.AddQueryHook(bunzap.NewQueryHook(bunzap.QueryHookOptions{
		Logger: zap.L(),
		//SlowDuration: 200 * time.Millisecond, // Omit to log all operations as debug
	}))

	return newBun, nil
}

// privates

func sqliteConn(ctx context.Context) (*Client, error) {
	// validate bun address has actually been set
	dbAddress := os.Getenv("DB_ADDRESS")
	if dbAddress == "" {
		return nil, fmt.Errorf("'%s' was not set when attempting to start sqlite", os.Getenv("DB_ADDRESS"))
	}

	// Drop anything fancy from DB address
	dbAddress = strings.Split(dbAddress, "?")[0]
	dbAddress = strings.TrimPrefix(dbAddress, "file:")

	// Append our own SQLite preferences
	dbAddress = "file:" + dbAddress + "?cache=shared"

	// Open new DB instance
	sqldb, err := sql.Open("sqlite", dbAddress)
	if err != nil {
		if errWithCode, ok := err.(*sqlite.Error); ok {
			err = errors.New(sqlite.ErrorCodeString[errWithCode.Code()])
		}
		return nil, fmt.Errorf("could not open sqlite bun: %s", err)
	}

	setConnectionValues(sqldb)

	if dbAddress == "file::memory:?cache=shared" {
		zap.L().Warn("sqlite in-memory database should only be used for debugging")
		// don't close connections on disconnect -- otherwise
		// the SQLite database will be deleted when there
		// are no active connections
		sqldb.SetConnMaxLifetime(0)
	}

	conn := getErrConn(bun.NewDB(sqldb, sqlitedialect.New()))

	// ping to check the bun is there and listening
	if err := conn.db.PingContext(ctx); err != nil {
		if errWithCode, ok := err.(*sqlite.Error); ok {
			err = errors.New(sqlite.ErrorCodeString[errWithCode.Code()])
		}
		return nil, fmt.Errorf("sqlite ping: %s", err)
	}

	zap.L().Info("connected to SQLITE database")
	return conn, nil
}

func pgConn(ctx context.Context) (*Client, error) {
	opts, err := deriveBunDBPGOptions()
	if err != nil {
		return nil, fmt.Errorf("could not doCreate bundb postgres options: %s", err)
	}

	sqldb := stdlib.OpenDB(*opts)

	setConnectionValues(sqldb)

	conn := getErrConn(bun.NewDB(sqldb, pgdialect.New()))

	// ping to check the bun is there and listening
	if err := conn.db.PingContext(ctx); err != nil {
		return nil, fmt.Errorf("postgres ping: %s", err)
	}

	zap.L().Info("connected to POSTGRES database")
	return conn, nil
}

func deriveBunDBPGOptions() (*pgx.ConnConfig, error) {
	// these are all optional, the bun adapter figures out defaults
	port := 5432
	address := os.Getenv("DB_ADDRESS")
	username := os.Getenv("DB_USER")
	password := os.Getenv("DB_PASSWORD")

	// validate database
	database := os.Getenv("DB_DATABASE")
	if database == "" {
		return nil, errors.New("no database set")
	}

	var tlsConfig *tls.Config
	tlsMode := os.Getenv("DB_TLS_MODE")
	switch tlsMode {
	case dbTLSModeDisable, dbTLSModeUnset:
		break // nothing to do
	case dbTLSModeEnable:
		/* #nosec G402 */
		tlsConfig = &tls.Config{
			InsecureSkipVerify: true,
		}
	case dbTLSModeRequire:
		tlsConfig = &tls.Config{
			InsecureSkipVerify: false,
			ServerName:         os.Getenv("DB_ADDRESS"),
			MinVersion:         tls.VersionTLS12,
		}
	}

	caCertPath := os.Getenv("DB_TLS_CA_CERT_PATH")
	if tlsConfig != nil && caCertPath != "" {
		// load the system cert pool first -- we'll append the given CA cert to this
		certPool, err := x509.SystemCertPool()
		if err != nil {
			return nil, fmt.Errorf("error fetching system CA cert pool: %s", err)
		}

		// open the file itself and make sure there's something in it
		/* #nosec G304 */
		caCertBytes, err := os.ReadFile(caCertPath)
		if err != nil {
			return nil, fmt.Errorf("error opening CA certificate at %s: %s", caCertPath, err)
		}
		if len(caCertBytes) == 0 {
			return nil, fmt.Errorf("ca cert at %s was empty", caCertPath)
		}

		// make sure we have a PEM block
		caPem, _ := pem.Decode(caCertBytes)
		if caPem == nil {
			return nil, fmt.Errorf("could not parse cert at %s into PEM", caCertPath)
		}

		// parse the PEM block into the certificate
		caCert, err := x509.ParseCertificate(caPem.Bytes)
		if err != nil {
			return nil, fmt.Errorf("could not parse cert at %s into x509 certificate: %s", caCertPath, err)
		}

		// we're happy, add it to the existing pool and then use this pool in our tls config
		certPool.AddCert(caCert)
		tlsConfig.RootCAs = certPool
	}

	cfg, _ := pgx.ParseConfig("")
	if address != "" {
		cfg.Host = address
	}
	if port > 0 {
		cfg.Port = uint16(port)
	}

	if username != "" {
		cfg.User = username
	}
	if password != "" {
		cfg.Password = password
	}
	if tlsConfig != nil {
		cfg.TLSConfig = tlsConfig
	}
	cfg.Database = database
	cfg.PreferSimpleProtocol = true
	cfg.RuntimeParams["application_name"] = "goml"

	return cfg, nil
}

// https://bun.uptrace.dev/postgres/running-bun-in-production.html#database-sql
func setConnectionValues(sqldb *sql.DB) {
	maxOpenConns := 4 * runtime.GOMAXPROCS(0)
	sqldb.SetMaxOpenConns(maxOpenConns)
	sqldb.SetMaxIdleConns(maxOpenConns)
}

func getErrConn(dbConn *bun.DB) *Client {
	var errProc func(error) Error
	switch dbConn.Dialect().Name() {
	case dialect.PG:
		errProc = processPostgresError
	case dialect.SQLite:
		errProc = processSQLiteError
	default:
		panic("unknown dialect name: " + dbConn.Dialect().Name().String())
	}
	return &Client{
		errProc: errProc,
		db:      dbConn,
	}
}

// Close closes the bun db connection.
func (c *Client) Close() Error {
	zap.L().Info("Closing db connection")

	return c.db.Close()
}

// DoMigration runs schema migrations on the database.
func (c *Client) DoMigration(ctx context.Context) Error {
	migrator := migrate.NewMigrator(c.db, migrations.Migrations)

	if err := migrator.Init(ctx); err != nil {
		return err
	}

	group, err := migrator.Migrate(ctx)
	if err != nil {
		if err.Error() == "migrate: there are no any migrations" {
			return nil
		}

		return err
	}

	if group.ID == 0 {
		zap.L().Info("No migrations to run")

		return nil
	}
	zap.L().Info("Migration successful", zap.String("group", group.String()))

	return nil
}
