package api

import (
	"context"
	"github.com/gin-gonic/gin"
	"net/http"
	"tyr.codes/tyr/LongshipLink/auth/models"
)

type serverRequest struct {
}

type serverResponse struct {
	Token  string `json:"token"`
	Secret string `json:"secret"`
}

func (a *API) newServerResponse(ctx context.Context, server *models.Server) (*serverResponse, error) {
	ctx, span := tracer.Start(ctx, "newServerResponse", tracerAttrs...)
	defer span.End()

	token, err := a.Logic.GenerateToken(ctx, server)
	if err != nil {
		return nil, err
	}

	return &serverResponse{
		Token:  token,
		Secret: server.Secret,
	}, nil
}

func (a *API) newServersResponse(ctx context.Context, server []*models.Server) ([]*serverResponse, error) {
	ctx, span := tracer.Start(ctx, "newServersResponse", tracerAttrs...)
	defer span.End()

	var serversList = make([]*serverResponse, len(server))

	for i, s := range server {
		s, err := a.newServerResponse(ctx, s)
		if err != nil {
			return nil, err
		}
		serversList[i] = s
	}

	return serversList, nil
}

func (a *API) serversGetHandler(c *gin.Context) {
	servers, err := a.Logic.GetServers(c.Request.Context())
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	sr, err := a.newServersResponse(c.Request.Context(), servers)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, gin.H{"servers": sr})
}

func (a *API) serversPostHandler(c *gin.Context) {
	var server models.Server
	if err := c.ShouldBindJSON(&server); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	err := a.Logic.CreateServer(c.Request.Context(), &server)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	sr, err := a.newServerResponse(c.Request.Context(), &server)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusCreated, gin.H{"server": sr})
}
