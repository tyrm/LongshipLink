package database

import (
	"context"
	"github.com/uptrace/bun"
	"tyr.codes/tyr/LongshipLink/auth/models"
)

func (c *Client) CreateServer(ctx context.Context, server *models.Server) error {
	ctx, span := tracer.Start(ctx, "CreateServer", tracerAttrs...)
	defer span.End()

	query := c.db.NewInsert().
		Model(server).
		ExcludeColumn("created_at", "updated_at")

	if _, err := query.Exec(ctx); err != nil {
		return c.ProcessError(err)
	}

	return nil

}

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

func (c *Client) ReadServers(ctx context.Context) ([]*models.Server, error) {
	ctx, span := tracer.Start(ctx, "ReadServer", tracerAttrs...)
	defer span.End()

	var servers []*models.Server
	query := newServersQ(c.db, &servers)

	if err := query.Scan(ctx); err != nil {
		return nil, c.ProcessError(err)
	}

	return servers, nil
}

func newServerQ(c bun.IDB, server *models.Server) *bun.SelectQuery {
	return c.
		NewSelect().
		Model(server)
}

func newServersQ(c bun.IDB, servers *[]*models.Server) *bun.SelectQuery {
	return c.
		NewSelect().
		Model(servers)
}
