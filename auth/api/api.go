package api

import (
	"github.com/gin-gonic/gin"
	"go.opentelemetry.io/contrib/instrumentation/github.com/gin-gonic/gin/otelgin"
	"net/http"
	"time"
	"tyr.codes/tyr/LongshipLink/auth/logic"
)

const serverTimeout = 120 * time.Second

type API struct {
	Logic *logic.Logic

	server *http.Server
}

func (a *API) Start() error {
	e := gin.Default()

	e.Use(otelgin.Middleware("LongshipLink"))

	e.GET("/api/v1/auth/user", a.authUserGetHandler)

	a.server = &http.Server{
		Addr:         "localhost:5420",
		Handler:      e,
		WriteTimeout: serverTimeout,
		ReadTimeout:  serverTimeout,
	}

	return a.server.ListenAndServe()
}

func (a *API) Close() error {
	return a.server.Close()
}
