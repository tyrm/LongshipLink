package logic

import (
	"context"
	pubnub "github.com/pubnub/go/v7"
	semconv "go.opentelemetry.io/otel/semconv/v1.24.0"
)

func pnUserUID(userID string) string {
	return "u:" + userID
}

func (l *Logic) pnGrantTokenReq(ctx context.Context, userID, serverID string) (*pubnub.PNGrantTokenResponse, pubnub.StatusResponse, error) {
	ctx, span := tracer.Start(ctx, "PNGrantToken", tracerAttrs...)
	span.SetAttributes(semconv.ServiceName("PubNub"))
	defer span.End()

	return l.pubnub.GrantTokenWithContext(ctx).
		TTL(15).
		AuthorizedUUID(pnUserUID(userID)).
		Channels(map[string]pubnub.ChannelPermissions{
			"presence.user": {
				Read: true,
				Get:  true,
			},
			"chats.global": {
				Read:  true,
				Write: true,
				Get:   true,
			},
		}).
		Meta(map[string]interface{}{
			"sid": serverID,
		}).
		Execute()
}
