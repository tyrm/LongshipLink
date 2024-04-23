package logic

import (
	"context"
	"errors"
	"net/http"
)

const tokenTTL = 61

func (l *Logic) GetUserAuthToken(ctx context.Context, serverID, userID string) (string, error) {
	ctx, span := tracer.Start(ctx, "GetUserAuthToken", tracerAttrs...)
	defer span.End()

	// create authorization token
	res, status, err := l.pnGrantTokenReq(ctx, userID, serverID, tokenTTL)
	if err != nil {
		span.RecordError(err)
		return "", err
	}

	if status.StatusCode != http.StatusOK {
		err := errors.New("invalid status code")
		span.RecordError(err)
		return "", err
	}

	return res.Data.Token, nil
}
