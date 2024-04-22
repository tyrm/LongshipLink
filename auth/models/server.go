package models

import "time"

type Server struct {
	ID        int64     `bun:",pk,autoincrement"`
	CreatedAt time.Time `bun:",nullzero,notnull,default:current_timestamp"`
	UpdatedAt time.Time `bun:",nullzero,notnull,default:current_timestamp"`

	Secret string `bun:",notnull,nullzero"`
}

func (s *Server) Kind() int {
	return KindServer
}
