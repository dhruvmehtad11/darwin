package loggerUtil

import (
	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
	"os"
	"strconv"
)

func CreateLogger(fileName string, levels ...string) *zap.Logger {
	var atomicLevels []zap.AtomicLevel

	for _, l := range levels {
		lvl := zap.NewAtomicLevel()
		if err := lvl.UnmarshalText([]byte(l)); err != nil {
			lvl.SetLevel(zap.InfoLevel)
		}
		atomicLevels = append(atomicLevels, lvl)
	}

	encoderCfg := zap.NewDevelopmentEncoderConfig()

	logger := zap.New(zapcore.NewTee(buildCores(encoderCfg, atomicLevels, fileName)...))
	return logger
}

// adds different cores to the logger for both file and console logging
func buildCores(encoderCfg zapcore.EncoderConfig, atomicLevels []zap.AtomicLevel, fileName string) []zapcore.Core {
	var cores []zapcore.Core

	consoleEncoder := zapcore.NewConsoleEncoder(encoderCfg)
	consoleOutput := zapcore.Lock(os.Stdout)

	for _, level := range atomicLevels {
		core := zapcore.NewCore(consoleEncoder, consoleOutput, level)
		cores = append(cores, core)
	}

	logToFile, _ := strconv.Atoi(os.Getenv("LOG_TO_FILE"))
	if logToFile == 1 {
		fileEncoder := zapcore.NewJSONEncoder(encoderCfg)
		fileOutput, err := os.OpenFile(fileName, os.O_CREATE|os.O_WRONLY|os.O_APPEND, 0644)
		if err != nil {
			fileOutput, _ = os.OpenFile("./cluster_manager.log", os.O_CREATE|os.O_WRONLY|os.O_APPEND, 0644)
		}
		fileSync := zapcore.AddSync(fileOutput)
		for _, level := range atomicLevels {
			core := zapcore.NewCore(fileEncoder, fileSync, level)
			cores = append(cores, core)
		}
	}

	return cores
}
