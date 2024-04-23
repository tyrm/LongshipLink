package api

import (
	"errors"
	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
	"net/http"
	"os"
	"tyr.codes/tyr/LongshipLink/auth/database"
)

type authUserGetResponse struct {
	SubscriptionKey string `json:"sub_key"`
	PublishKey      string `json:"pub_key"`
	Token           string `json:"token"`
}

func (a *API) authUserGetHandler(c *gin.Context) {
	serverID := c.Query("sid")
	secret := c.Query("secret")
	userID := c.Query("uid")

	zap.L().Debug("authUserGetHandler", zap.String("server_id", serverID), zap.String("secret", secret), zap.String("user_id", userID))

	// decode token
	serverIDInt, err := a.Logic.DecodeToken(c.Request.Context(), serverID)
	if err != nil {
		zap.L().Debug("Error decoding token", zap.Error(err))
		c.JSON(http.StatusUnauthorized, gin.H{"error": http.StatusText(http.StatusUnauthorized)}) // return generic error before server auth
		return
	}

	// get server
	server, err := a.Logic.GetServer(c.Request.Context(), serverIDInt)
	if err != nil {
		if errors.Is(err, database.ErrNoEntries) {
			c.JSON(http.StatusUnauthorized, gin.H{"error": http.StatusText(http.StatusUnauthorized)}) // return generic error before server auth
			return
		} else {
			c.JSON(http.StatusInternalServerError, gin.H{"error": http.StatusText(http.StatusInternalServerError)}) // return generic error before server auth
			return
		}
	}

	// check if server secret matches
	if server.Secret != secret {
		c.JSON(http.StatusUnauthorized, gin.H{"error": http.StatusText(http.StatusUnauthorized)}) // return generic error before server auth
		return
	}

	// check if user exists in playerdb
	if a.ValidatePlayer {
		playerExists, err := a.Logic.MinecraftPlayerExists(c.Request.Context(), userID)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
			return
		}

		if !playerExists {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "player does not exist"})
			return
		}
	}

	// create authorization token
	token, err := a.Logic.GetUserAuthToken(c.Request.Context(), serverID, userID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(200, authUserGetResponse{
		SubscriptionKey: os.Getenv("SUB_KEY"),
		PublishKey:      os.Getenv("PUB_KEY"),
		Token:           token,
	})
}
