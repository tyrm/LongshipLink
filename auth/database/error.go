package database

import (
	"database/sql"
	"errors"
	"fmt"
	"github.com/jackc/pgconn"
	"go.uber.org/zap"
	"modernc.org/sqlite"
	sqlite3 "modernc.org/sqlite/lib"
)

// Error represents a database specific error.
type Error error

var (
	// ErrGenID is returned when creating a new ID can't be generated for a new model.
	ErrGenID Error = fmt.Errorf("can't generate id")
	// ErrNoEntries is returned when a caller expected an entry for a query, but none was found.
	ErrNoEntries Error = fmt.Errorf("no entries")
	// ErrMultipleEntries is returned when a caller expected ONE entry for a query, but multiples were found.
	ErrMultipleEntries Error = fmt.Errorf("multiple entries")
	// ErrUnknown denotes an unknown database error.
	ErrUnknown Error = fmt.Errorf("unknown error")
	// ErrInvalidSort is returned when a sort type is requested the model can't do.
	ErrInvalidSort Error = fmt.Errorf("invalid sort")
)

// AlreadyExistsError is returned when a caller tries to insert a database entry that already exists in the db.
type AlreadyExistsError struct {
	message string
}

// Error returns the error message as a string.
func (e *AlreadyExistsError) Error() string {
	return e.message
}

// NewErrAlreadyExists wraps a message in an AlreadyExistsError object.
func NewErrAlreadyExists(msg string) error {
	return &AlreadyExistsError{message: msg}
}

// ProcessError replaces any known values with our own db.Error types
func (c *Client) ProcessError(err error) Error {
	switch {
	case err == nil:
		return nil
	case errors.Is(err, sql.ErrNoRows):
		return ErrNoEntries
	default:
		return c.errProc(err)
	}
}

// processPostgresError processes an error, replacing any postgres specific errors with our own error type
func processPostgresError(err error) Error {
	// Attempt to cast as postgres
	var pgErr *pgconn.PgError
	ok := errors.As(err, &pgErr)
	if !ok {
		return err
	}

	zap.L().Debug("Postgres error", zap.String("code", pgErr.Code), zap.Error(pgErr))

	// Handle supplied error code:
	// (https://www.postgresql.org/docs/10/errcodes-appendix.html)
	switch pgErr.Code {
	case "23505" /* unique_violation */ :
		return NewErrAlreadyExists(pgErr.Message)
	default:
		return err
	}
}

// processSQLiteError processes an error, replacing any sqlite specific errors with our own error type

func processSQLiteError(err error) Error {
	// Attempt to cast as sqlite
	var sqliteErr *sqlite.Error
	ok := errors.As(err, &sqliteErr)
	if !ok {
		return err
	}

	zap.L().Debug("Postgres error", zap.Int("code", sqliteErr.Code()), zap.Error(sqliteErr))
	// Handle supplied error code:
	switch sqliteErr.Code() {
	case sqlite3.SQLITE_CONSTRAINT_UNIQUE, sqlite3.SQLITE_CONSTRAINT_PRIMARYKEY:
		return NewErrAlreadyExists(err.Error())
	default:
		return err
	}
}
