package tracer_utils

import (
	"compute/cluster_manager/utils/logger"
	"context"
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/attribute"
	"go.opentelemetry.io/otel/exporters/otlp/otlptrace"
	"go.opentelemetry.io/otel/exporters/otlp/otlptrace/otlptracegrpc"
	"go.opentelemetry.io/otel/propagation"
	"go.opentelemetry.io/otel/sdk/resource"
	sdktrace "go.opentelemetry.io/otel/sdk/trace"
	"go.uber.org/zap"
	"os"
)

var (
	serviceName  = os.Getenv("SERVICE_NAME")
	version      = os.Getenv("ARTIFACT_VERSION")
	env          = os.Getenv("ENV")
	collectorURL = os.Getenv("OTEL_EXPORTER_OTLP_ENDPOINT")
)

// InitTracer Using glog as logger as datadog is not able to parse zap logs
func InitTracer() func(context.Context) error {
	secureOption := otlptracegrpc.WithInsecure()
	logger.Info("collectorURL", zap.String("collectorURL", collectorURL))

	exporter, err := otlptrace.New(
		context.Background(),
		otlptracegrpc.NewClient(
			secureOption,
			otlptracegrpc.WithEndpoint(collectorURL),
		),
	)
	if err != nil {
		logger.Fatal("", zap.Error(err))
	}

	resources, err := resource.New(
		context.Background(),
		resource.WithAttributes(
			attribute.String("service.name", serviceName),
			attribute.String("library.language", "go"),
			attribute.String("service.version", version),
			attribute.String("env", env),
		),
	)
	if err != nil {
		logger.Fatal("Could not set resources: ", zap.Error(err))
	}

	otel.SetTracerProvider(
		sdktrace.NewTracerProvider(
			sdktrace.WithSampler(sdktrace.AlwaysSample()),
			sdktrace.WithBatcher(exporter),
			sdktrace.WithResource(resources),
		),
	)

	otel.SetTextMapPropagator(
		propagation.NewCompositeTextMapPropagator(
			propagation.TraceContext{},
			propagation.Baggage{},
		),
	)
	return exporter.Shutdown
}
