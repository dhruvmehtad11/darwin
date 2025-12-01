from compute_core.util.utils import add_list_to_query


def test_add_list_to_query_empty_list():
    query = """SELECT *
FROM library WHERE id IN """
    query_list = []
    resp = add_list_to_query(query=query, query_list=query_list)
    expected = f"{query}()"
    assert resp == expected


def test_add_list_to_query_one_element_list():
    query = """SELECT *
    FROM library WHERE id IN """
    # list of int
    query_list = [1]
    resp = add_list_to_query(query=query, query_list=query_list)
    expected = f"{query}(1)"
    assert resp == expected

    # list of str
    query_list = ["1"]
    resp = add_list_to_query(query=query, query_list=query_list)
    expected = f"{query}('1')"
    assert resp == expected


def test_add_list_to_query_multiple_element_list():
    query = """SELECT *
    FROM library WHERE id IN """
    # list of int
    query_list = [1, 2]
    resp = add_list_to_query(query=query, query_list=query_list)
    print(resp)
    expected = f"{query}(1, 2)"
    assert resp == expected

    # list of string
    query_list = ["1", "2"]
    resp = add_list_to_query(query=query, query_list=query_list)
    expected = f"{query}('1', '2')"
    assert resp == expected
