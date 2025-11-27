package helm_utils

import (
	"gopkg.in/yaml.v2"
	"helm.sh/helm/v3/pkg/chart"
	"helm.sh/helm/v3/pkg/chart/loader"
	"helm.sh/helm/v3/pkg/chartutil"
)

func UpdateChartValues(chart *chart.Chart, vals map[string]interface{}) error {
	var err error

	// Update the values file in the memory
	chart.Values = vals

	// Update the values file in the chart, so that new values can be packaged
	for _, file := range chart.Raw {
		if file.Name == "values.yaml" {
			file.Data, err = yaml.Marshal(vals)
			if err != nil {
				return err
			}
		}
	}
	return nil
}

// PackageWithValues Run executes 'helm package' against the given chart with the given values and returns the path to the packaged chart.
func PackageWithValues(chartPath string, values map[string]interface{}, destinationPath string) (string, error) {
	loadedChart, err := loader.LoadDir(chartPath)
	if err != nil {
		return "", err
	}

	if err = UpdateChartValues(loadedChart, values); err != nil {
		return "", err
	}

	name, err := chartutil.Save(loadedChart, destinationPath)
	if err != nil {
		return "", err
	}

	return name, nil
}
