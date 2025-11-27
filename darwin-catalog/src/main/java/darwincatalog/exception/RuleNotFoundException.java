package darwincatalog.exception;

public class RuleNotFoundException extends AssetException {
  public RuleNotFoundException(String assetName, Long ruleId) {
    super(String.format("Rule with id %s does not exist for asset %s", ruleId, assetName));
  }
}
