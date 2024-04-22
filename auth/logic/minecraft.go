package logic

import (
	"context"
	"errors"
	"tyr.codes/libs/playerdb"
)

func (l *Logic) MinecraftPlayerExists(ctx context.Context, userID string) (bool, error) {
	ctx, span := tracer.Start(ctx, "MinecraftPlayerExists", tracerAttrs...)
	defer span.End()

	_, err := l.PlayerDB.Minecraft(ctx, userID)
	if err != nil {
		if errors.Is(err, playerdb.ErrMinecraftInvalidUsername) {
			// player does not exist
			return false, nil
		}

		// some other error
		span.RecordError(err)
		return false, err
	}

	// player exists
	return true, nil
}
