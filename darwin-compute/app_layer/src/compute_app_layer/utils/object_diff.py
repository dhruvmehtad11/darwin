from deepdiff import DeepDiff


def get_diff(r1, r2):
    diff = DeepDiff(r1, r2, ignore_order=True)
    return diff
