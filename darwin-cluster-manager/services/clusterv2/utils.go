package clusterv2

func CreateChartPath(localPath string, artifactName string, prefix string) string {
	return localPath + prefix + artifactName
}
