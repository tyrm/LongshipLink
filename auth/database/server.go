package database

import (
	"context"
	"github.com/uptrace/bun"
	"tyr.codes/tyr/LongshipLink/auth/models"
)

func (c *Client) ReadServer(ctx context.Context, id int64) (*models.Server, error) {
	ctx, span := tracer.Start(ctx, "ReadServer", tracerAttrs...)
	defer span.End()

	server := &models.Server{
		ID: id,
	}
	query := newServerQ(c.db, server).
		WherePK()

	if err := query.Scan(ctx); err != nil {
		return nil, c.ProcessError(err)
	}

	return server, nil
}

func newServerQ(c bun.IDB, server *models.Server) *bun.SelectQuery {
	return c.
		NewSelect().
		Model(server)
}
