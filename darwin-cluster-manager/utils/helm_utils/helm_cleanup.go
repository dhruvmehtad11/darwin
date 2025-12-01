package helm_utils

import (
	"compute/cluster_manager/utils/logger"
	"context"
	"fmt"
	"go.uber.org/zap"
	v1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/tools/clientcmd"
	"sort"
	"strings"
)

type helmSecret struct {
	Name     string
	Revision int
}

func DeleteOldSecrets(clientSet *kubernetes.Clientset, namespace string, secrets *v1.SecretList, maxHistory int) {
	var secretList []helmSecret
	for _, secret := range secrets.Items {
		// Helm secret names are in format: sh.helm.release.v1.<release-name>.v<revision>
		parts := strings.Split(secret.Name, ".v")
		if len(parts) < 2 {
			continue
		}

		var rev int
		_, err := fmt.Sscanf(parts[len(parts)-1], "%d", &rev)
		if err == nil {
			secretList = append(secretList, helmSecret{Name: secret.Name, Revision: rev})
		}
	}

	sort.Slice(secretList, func(i, j int) bool {
		return secretList[i].Revision < secretList[j].Revision
	})

	// Delete only the old revisions
	deleteCount := len(secretList) - maxHistory
	for i := 0; i < deleteCount; i++ {
		oldSecret := secretList[i]
		logger.Info(fmt.Sprintf("Deleting old revision %d secret: %s", oldSecret.Revision, oldSecret.Name))

		err := clientSet.CoreV1().Secrets(namespace).Delete(context.TODO(), oldSecret.Name, metav1.DeleteOptions{})
		if err != nil {
			logger.Error("Failed to delete revision %d secret: %v", zap.Int("revision", oldSecret.Revision), zap.Error(err))
		}
	}
}

func GetSecrets(clientSet *kubernetes.Clientset, namespace string, releaseName string) (*v1.SecretList, error) {
	secrets, err := clientSet.CoreV1().Secrets(namespace).List(context.TODO(), metav1.ListOptions{
		LabelSelector: fmt.Sprintf("name=%s,owner=helm,status in (superseded,failed,uninstalled)", releaseName),
	})

	if err != nil {
		logger.Error("failed to list secrets for release: %s", zap.String("releaseName", releaseName), zap.Error(err))
		return nil, err
	}

	return secrets, nil
}

func GetKubeClientSet(kubeConfigPath string) (*kubernetes.Clientset, error) {
	config, err := clientcmd.BuildConfigFromFlags("", kubeConfigPath)
	if err != nil {
		logger.Error("failed to build kube config", zap.Error(err))
		return nil, err
	}

	clientSet, err := kubernetes.NewForConfig(config)
	if err != nil {
		logger.Error("failed to create k8s clientset", zap.Error(err))
		return nil, err
	}

	return clientSet, nil
}

func CleanupOldHelmSecrets(releaseName string, namespace string, kubeConfigPath string, maxHistory int) error {
	clientSet, err := GetKubeClientSet(kubeConfigPath)

	if err != nil {
		logger.Error("failed to get kube clientset", zap.Error(err))
		return err
	}

	secrets, err := GetSecrets(clientSet, namespace, releaseName)

	if err != nil {
		logger.Error("failed to list secrets for release: %s", zap.String("releaseName", releaseName), zap.Error(err))
		return err
	}

	if len(secrets.Items) <= maxHistory {
		logger.Info(fmt.Sprintf("No old secrets to cleanup for release: %s", releaseName))
		return nil
	}

	DeleteOldSecrets(clientSet, namespace, secrets, maxHistory)

	return nil
}
