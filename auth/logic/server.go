package logic

import (
	"context"
	"errors"
	"go.uber.org/zap"
	"tyr.codes/tyr/LongshipLink/auth/database"
	"tyr.codes/tyr/LongshipLink/auth/models"
	"tyr.codes/tyr/LongshipLink/auth/util"
)

func (l *Logic) GetServer(ctx context.Context, id int64) (*models.Server, error) {
	ctx, span := tracer.Start(ctx, "GetServer", tracerAttrs...)
	defer span.End()

	server, err := l.DB.ReadServer(ctx, id)
	if err != nil {
		if !errors.Is(err, database.ErrNoEntries) {
			span.RecordError(err)
		}
		zap.L().Debug("Error getting server", zap.Error(err))
		return nil, err
	}

	return server, nil
}

func (l *Logic) GetServers(ctx context.Context) ([]*models.Server, error) {
	ctx, span := tracer.Start(ctx, "GetServers", tracerAttrs...)
	defer span.End()

	servers, err := l.DB.ReadServers(ctx)
	if err != nil {
		if !errors.Is(err, database.ErrNoEntries) {
			span.RecordError(err)
		}
		zap.L().Debug("Error getting server", zap.Error(err))
		return nil, err
	}

	return servers, nil
}

func (l *Logic) CreateServer(ctx context.Context, server *models.Server) error {
	ctx, span := tracer.Start(ctx, "CreateServer", tracerAttrs...)
	defer span.End()

	if server.ID != 0 {
		err := errors.New("cannot create server with ID")
		span.RecordError(err)
		return err
	}

	server.Secret = util.RandomString(32)

	err := l.DB.CreateServer(ctx, server)
	if err != nil {
		span.RecordError(err)
		zap.L().Debug("Error creating server", zap.Error(err))
		return err
	}

	return nil
}
