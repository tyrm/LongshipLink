package logic

import (
	pubnub "github.com/pubnub/go/v7"
	"github.com/speps/go-hashids/v2"
	"go.uber.org/zap"
	"net/http"
	"os"
	"tyr.codes/libs/playerdb"
	"tyr.codes/tyr/LongshipLink/auth/database"
	"tyr.codes/tyr/LongshipLink/auth/httpclient"
)

const minTokenLength = 16

type Logic struct {
	DB       *database.Client
	HTTP     *http.Client
	PlayerDB *playerdb.Client
	hashid   *hashids.HashID
	pubnub   *pubnub.PubNub
}

func NewLogic(db *database.Client) *Logic {
	// create new http client
	client := httpclient.New()

	pnconfig := pubnub.NewConfigWithUserId("auth-server")
	pnconfig.SubscribeKey = os.Getenv("SUB_KEY")
	pnconfig.PublishKey = os.Getenv("PUB_KEY")
	pnconfig.SecretKey = os.Getenv("SEC_KEY")

	// setup playerdb
	pdb := playerdb.NewClient()
	pdb.HttpClient = client

	// setup hashid
	hd := hashids.NewData()
	hd.Salt = "test"
	hd.MinLength = minTokenLength

	hid, err := hashids.NewWithData(hd)
	if err != nil {
		zap.L().Fatal("Error creating hashid", zap.Error(err))
	}

	return &Logic{
		DB:       db,
		PlayerDB: pdb,
		HTTP:     client,

		hashid: hid,
		pubnub: pubnub.NewPubNub(pnconfig),
	}
}
