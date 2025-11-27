import unittest

from compute_model.cluster_status import ClusterStatus
from compute_script.dto.cluster_status_graph import (
    generate_status_graph,
    ClusterStatusGraph,
    TransitionAction,
    ChronosEventAction,
)


class TestClusterStatusGraph(unittest.TestCase):
    def setUp(self):
        self.graph = ClusterStatusGraph()
        self.start = ClusterStatus.creating
        self.middle = ClusterStatus.head_node_up
        self.end = ClusterStatus.active
        self.failed = ClusterStatus.cluster_died

    def test_add_status(self):
        self.graph.add_status(self.start)
        self.assertIn(self.start, self.graph.graph)

    def test_add_transition(self):
        actions = [TransitionAction(action="start_to_middle", message="Start to Middle")]
        chronos_event_actions = [ChronosEventAction(event_type="START", message="Starting")]
        self.graph.add_transition(self.start, self.middle, actions, chronos_event_actions)
        self.assertIn(self.middle, self.graph.graph[self.start])
        self.assertEqual(self.graph.transition_actions[(self.start, self.middle)], actions)
        self.assertEqual(
            self.graph.chronos_event_actions[(self.start, self.middle)],
            chronos_event_actions,
        )

    def test_add_transitions(self):
        actions1 = [TransitionAction(action="start_to_middle", message="Start to Middle")]
        chronos_event_actions1 = [ChronosEventAction(event_type="START", message="Starting")]
        actions2 = [TransitionAction(action="middle_to_end", message="Middle to End")]
        chronos_event_actions2 = [ChronosEventAction(event_type="PROGRESS", message="In Progress")]
        self.graph.add_transitions(
            self.start,
            [
                (self.middle, actions1, chronos_event_actions1),
                (self.end, actions2, chronos_event_actions2),
            ],
        )
        self.assertIn(self.middle, self.graph.graph[self.start])
        self.assertIn(self.end, self.graph.graph[self.start])
        self.assertEqual(self.graph.transition_actions[(self.start, self.middle)], actions1)
        self.assertEqual(
            self.graph.chronos_event_actions[(self.start, self.middle)],
            chronos_event_actions1,
        )
        self.assertEqual(self.graph.transition_actions[(self.start, self.end)], actions2)
        self.assertEqual(
            self.graph.chronos_event_actions[(self.start, self.end)],
            chronos_event_actions2,
        )

    def test_get_next_status(self):
        self.graph.add_status(self.start)
        self.assertEqual(self.graph.get_next_status(self.start), [])
        self.graph.add_transition(self.start, self.middle)
        self.assertEqual(self.graph.get_next_status(self.start), [self.middle])

    def test_shortest_path(self):
        self.graph.add_transition(self.start, self.middle)
        self.graph.add_transition(self.middle, self.end)
        self.assertEqual(
            self.graph.shortest_path(self.start, self.end),
            [self.start, self.middle, self.end],
        )
        self.assertEqual(self.graph.shortest_path(self.start, self.failed), [])

    def test_get_transition_actions(self):
        actions = [TransitionAction(action="start_to_middle", message="Start to Middle")]
        self.graph.add_transition(self.start, self.middle, actions)
        self.assertEqual(self.graph.get_transition_actions(self.start, self.middle), actions)

    def test_get_chronos_event_actions(self):
        chronos_event_actions = [ChronosEventAction(event_type="START", message="Starting")]
        self.graph.add_transition(self.start, self.middle, chronos_event_actions=chronos_event_actions)
        self.assertEqual(
            self.graph.get_chronos_event_actions(self.start, self.middle),
            chronos_event_actions,
        )


def test_generate_status_graph():
    generate_status_graph()
    assert True
