package logger

import (
	"compute/cluster_manager/constants"
	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
	"os"
)

var (
	defaultLogFile = "./cluster_manager.log"
	logFile        = os.Getenv("LOG_FILE")
	Log            *zap.Logger
)

// Build Core to the logger for both file and console logging
func buildCore(fileName string) zapcore.Core {
	var cores []zapcore.Core

	encoderCfg := zap.NewProductionEncoderConfig()
	encoderCfg.EncodeTime = zapcore.ISO8601TimeEncoder

	consoleEncoder := zapcore.NewConsoleEncoder(encoderCfg)
	consoleOutput := zapcore.Lock(os.Stdout)
	if constants.ENV != "prod" && constants.ENV != "uat" {
		cores = append(cores, zapcore.NewCore(consoleEncoder, consoleOutput, zapcore.DebugLevel))
	}

	fileEncoder := zapcore.NewJSONEncoder(encoderCfg)
	fileOutput, err := os.OpenFile(fileName, os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0644)
	if err != nil {
		fileOutput, _ = os.OpenFile(defaultLogFile, os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0644)
	}
	fileSync := zapcore.AddSync(fileOutput)
	cores = append(cores, zapcore.NewCore(fileEncoder, fileSync, zapcore.DebugLevel))

	core := zapcore.NewTee(cores...)
	return core
}

func Init() {
	if logFile == "" {
		logFile = defaultLogFile
	}
	Log = zap.New(buildCore(logFile), zap.AddCaller(), zap.AddCallerSkip(1), zap.AddStacktrace(zap.ErrorLevel))
	zap.ReplaceGlobals(Log)
}

func Info(msg string, fields ...zapcore.Field) {
	Log.Info(msg, fields...)
}

func Debug(msg string, fields ...zapcore.Field) {
	Log.Debug(msg, fields...)
}

func Error(msg string, fields ...zapcore.Field) {
	Log.Error(msg, fields...)
}

func Fatal(msg string, fields ...zapcore.Field) {
	Log.Fatal(msg, fields...)
}

func InfoR(requestId string, msg string, fields ...zapcore.Field) {
	Log.Info(msg, append(fields, zap.String("requestID", requestId))...)
}

func DebugR(requestId string, msg string, fields ...zapcore.Field) {
	Log.Debug(msg, append(fields, zap.String("requestID", requestId))...)
}

func ErrorR(requestId string, msg string, fields ...zapcore.Field) {
	Log.Error(msg, append(fields, zap.String("requestID", requestId))...)
}

func Field(key string, value interface{}) zapcore.Field {
	return zap.Any(key, value)
}
