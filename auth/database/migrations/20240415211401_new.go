package migrations

import (
	"context"
	"github.com/uptrace/bun"
	"tyr.codes/libs/libmigration"
	models "tyr.codes/tyr/LongshipLink/auth/database/migrations/20240415211401_new"
)

func init() {
	addTables := libmigration.TableList{
		{
			Model: &models.Server{},
		},
		{
			Model: &models.Player{},
		},
	}

	addIndexes := libmigration.IndexList{
		{
			Model:   (*models.Player)(nil),
			Name:    "player_uid_unique_idx",
			Columns: []string{"uid"},
			Unique:  true,
		},
	}

	up := func(ctx context.Context, db *bun.DB) error {
		return db.RunInTx(ctx, nil, func(ctx context.Context, tx bun.Tx) error {
			if err := libmigration.AddTablesUp(ctx, tx, addTables); err != nil {
				return err
			}

			if err := libmigration.AddIndexesUp(ctx, tx, addIndexes); err != nil {
				return err
			}

			return nil
		})
	}

	down := func(ctx context.Context, db *bun.DB) error {
		return db.RunInTx(ctx, nil, func(ctx context.Context, tx bun.Tx) error {
			if err := libmigration.AddIndexesDown(ctx, tx, addIndexes); err != nil {
				return err
			}

			if err := libmigration.AddTablesDown(ctx, tx, addTables); err != nil {
				return err
			}

			return nil
		})
	}

	if err := Migrations.Register(up, down); err != nil {
		panic(err)
	}
}
