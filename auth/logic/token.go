package logic

import (
	"context"
	"errors"
	"github.com/gin-gonic/gin"
	pubnub "github.com/pubnub/go/v7"
	"go.uber.org/zap"
	"net/http"
	"tyr.codes/tyr/LongshipLink/auth/models"
)

func (l *Logic) DecodeToken(ctx context.Context, token string) (int64, error) {
	ctx, span := tracer.Start(ctx, "DecodeToken", tracerAttrs...)
	defer span.End()

	ints, err := l.hashid.DecodeWithError(token)
	if err != nil {
		zap.L().Debug("Error decoding token", zap.Error(err))
		return 0, err
	}

	if len(ints) != 2 {
		zap.L().Debug("Invalid token length")
		return 0, errors.New("invalid token length")
	}

	if ints[0] != models.KindServer {
		zap.L().Debug("Invalid token kind")
		return 0, errors.New("invalid token kind")
	}

	return int64(ints[1]), nil
}

func (l *Logic) GenerateToken(ctx context.Context, server *models.Server) (string, error) {
	ctx, span := tracer.Start(ctx, "GenerateToken", tracerAttrs...)
	defer span.End()

	token, err := l.hashid.Encode([]int{models.KindServer, int(server.ID)})
	if err != nil {
		zap.L().Debug("Error encoding token", zap.Error(err))
		return "", err
	}

	return token, nil
}

func (l *Logic) ParseAuthToken(c *gin.Context, token string) {
	_, span := tracer.Start(c.Request.Context(), "ParseAuthToken", tracerAttrs...)
	defer span.End()

	tokenData, err := pubnub.ParseToken(token)
	if err != nil {
		return
	}
	c.JSON(http.StatusOK, tokenData)
}
