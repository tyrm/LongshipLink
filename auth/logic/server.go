package logic

import (
	"context"
	"go.uber.org/zap"
	"tyr.codes/tyr/LongshipLink/auth/models"
)

func (l *Logic) GetServer(ctx context.Context, id int64) (*models.Server, error) {
	ctx, span := tracer.Start(ctx, "GetServer", tracerAttrs...)
	defer span.End()

	server, err := l.DB.ReadServer(ctx, id)
	if err != nil {
		zap.L().Debug("Error getting server", zap.Error(err))
		return nil, err
	}

	return server, nil
}
