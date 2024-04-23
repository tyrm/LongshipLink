package main

import (
	"context"
	"github.com/uptrace/uptrace-go/uptrace"
	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
	"tyr.codes/tyr/LongshipLink/auth/api"
	"tyr.codes/tyr/LongshipLink/auth/database"
	"tyr.codes/tyr/LongshipLink/auth/logic"
)

func main() {
	// Set up logger
	config := zap.NewDevelopmentConfig()
	config.EncoderConfig.EncodeLevel = zapcore.CapitalColorLevelEncoder
	config.Level.SetLevel(zapcore.DebugLevel) // Set the log level here
	logger, err := config.Build()
	if err != nil {
		panic(err)
	}
	defer logger.Sync()

	zap.ReplaceGlobals(logger)

	uptrace.ConfigureOpentelemetry(
		uptrace.WithServiceName("LongshipLink Authentication Service"),
		uptrace.WithServiceVersion("dev"),
	)

	// Set up context
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	// Create database client
	zap.L().Debug("Creating database client")
	db, err := database.New(ctx)
	if err != nil {
		logger.Fatal("Error creating database client", zap.Error(err))
	}
	defer db.Close()

	zap.L().Info("Running database migration")
	err = db.DoMigration(ctx)
	if err != nil {
		zap.L().Fatal("Error running migration", zap.Error(err))
	}

	// Create API
	a := api.API{
		Logic:          logic.NewLogic(db),
		ValidatePlayer: false,
	}

	// Start API
	zap.L().Info("Starting API")
	if err := a.Start(); err != nil {
		logger.Fatal("Error starting API", zap.Error(err))
	}
}
