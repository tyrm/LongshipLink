package logic

import (
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/attribute"
	"go.opentelemetry.io/otel/trace"
)

var tracer = otel.Tracer("logic")
var tracerAttrs = []trace.SpanStartOption{
	trace.WithAttributes(
		attribute.String("struct", "Logic"),
	),
}
