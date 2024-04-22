package models

import "time"

type Player struct {
	ID        int64     `bun:",pk,autoincrement"`
	CreatedAt time.Time `bun:",nullzero,notnull,default:current_timestamp"`
	UpdatedAt time.Time `bun:",nullzero,notnull,default:current_timestamp"`

	UID    string `bun:",nullzero,notnull"`
	Secret string `bun:",nullzero,notnull"`
}
