package httpclient

import (
	"fmt"
	"go.opentelemetry.io/contrib/instrumentation/net/http/otelhttp"
	"net/http"
	"runtime"
	"time"
)

const (
	application = "LongshipLink"
	version     = "0.0.1"
	endpoint    = "http://localhost:5420"
)

func New() *http.Client {
	userAgent := fmt.Sprintf("%s/%s (compatible; Go-http-client/2.0; %s; %s; %s) Info: %s", application, version, runtime.Version(), runtime.GOOS, runtime.GOARCH, endpoint)

	return &http.Client{
		Timeout: time.Second * 10,
		Transport: otelhttp.NewTransport(&transport{
			RoundTripper: http.DefaultTransport,
			userAgent:    userAgent,
		}),
	}
}

// transport is a standard client with updated user agent
type transport struct {
	http.RoundTripper
	userAgent string
}

// RoundTrip executes the default http.Transport with expected http User-Agent.
func (t *transport) RoundTrip(req *http.Request) (*http.Response, error) {
	req.Header.Set("User-Agent", t.userAgent)

	return t.RoundTripper.RoundTrip(req)
}
