package api

import (
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/attribute"
	"go.opentelemetry.io/otel/trace"
)

var tracer = otel.Tracer("database")
var tracerAttrs = []trace.SpanStartOption{
	trace.WithAttributes(
		attribute.String("struct", "API"),
	),
}
