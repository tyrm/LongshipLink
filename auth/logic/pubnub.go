package logic

import (
	"context"
	pubnub "github.com/pubnub/go/v7"
	semconv "go.opentelemetry.io/otel/semconv/v1.24.0"
)

func (l *Logic) pnGrantTokenReq(ctx context.Context, userID, serverID string, ttl int) (*pubnub.PNGrantTokenResponse, pubnub.StatusResponse, error) {
	ctx, span := tracer.Start(ctx, "PNGrantToken", tracerAttrs...)
	span.SetAttributes(semconv.ServiceName("PubNub"))
	defer span.End()

	return l.pubnub.GrantTokenWithContext(ctx).
		TTL(ttl).
		AuthorizedUUID(userID).
		Channels(map[string]pubnub.ChannelPermissions{
			"global": {
				Read:  true,
				Write: true,
				Get:   true,
			},
			"global-pnpres": {
				Read: true,
				Get:  true,
			},
		}).
		Meta(map[string]interface{}{
			"sid": serverID,
		}).
		Execute()
}
