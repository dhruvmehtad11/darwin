from collections import deque
from dataclasses import dataclass

from compute_model.cluster_status import ClusterStatus
from compute_script.constant.constants import (
    CLUSTER_DIED_ACTION,
    HEAD_NODE_UP_ACTION,
    HEAD_NODE_DIED_ACTION,
    WORKER_NODES_UP_ACTION,
    ACTIVE_ACTION,
    WORKER_NODES_DIED_ACTION,
    WORKER_NODES_SCALED_ACTION,
    CLUSTER_INACTIVE_ACTION,
)
from compute_script.constant.event_states import MonitoringState


@dataclass
class TransitionAction:
    action: str
    message: str


@dataclass
class ChronosEventAction:
    event_type: str
    message: str


class ClusterStatusGraph:
    def __init__(self):
        self.graph: dict[ClusterStatus, list[ClusterStatus]] = {}
        self.transition_actions: dict[(ClusterStatus, ClusterStatus), list[TransitionAction]] = {}
        self.chronos_event_actions: dict[(ClusterStatus, ClusterStatus), list[ChronosEventAction]] = {}

    def add_status(self, status: ClusterStatus) -> None:
        if status not in self.graph:
            self.graph[status] = []

    def add_transition(
        self,
        from_status: ClusterStatus,
        to_status: ClusterStatus,
        actions: list[TransitionAction] = None,
        chronos_event_actions: list[ChronosEventAction] = None,
    ) -> None:
        self.add_status(from_status)
        self.add_status(to_status)
        self.graph[from_status].append(to_status)
        self.transition_actions[(from_status, to_status)] = actions
        self.chronos_event_actions[(from_status, to_status)] = chronos_event_actions

    def add_transitions(
        self,
        from_status,
        to_and_actions: list[(ClusterStatus, list[TransitionAction], list[ChronosEventAction])],
    ) -> None:
        for to_status, actions, chronos_event_actions in to_and_actions:
            self.add_transition(from_status, to_status, actions, chronos_event_actions)

    def get_next_status(self, current_status: ClusterStatus) -> list[ClusterStatus]:
        return self.graph.get(current_status, [])

    def print_status_graph(self) -> None:
        for k, v in self.graph.items():
            print(f"{k.name} -> {', '.join([status.name for status in v])}")

    def shortest_path(self, start_status: ClusterStatus, end_status: ClusterStatus) -> list[ClusterStatus]:
        queue = deque([start_status])
        path = {status: [] for status in ClusterStatus}
        path[start_status] = [start_status]

        while queue:
            current_status = queue.popleft()
            if current_status == end_status:
                return path[current_status]

            for next_status in self.get_next_status(current_status):
                if not path[next_status]:
                    path[next_status] = path[current_status] + [next_status]
                    queue.append(next_status)

        return []

    def get_transition_actions(self, from_status: ClusterStatus, to_status: ClusterStatus) -> list[TransitionAction]:
        path = self.shortest_path(from_status, to_status)
        actions = []
        for i in range(len(path) - 1):
            actions += self.transition_actions[(path[i], path[i + 1])]
        return actions

    def get_chronos_event_actions(
        self, from_status: ClusterStatus, to_status: ClusterStatus
    ) -> list[ChronosEventAction]:
        path = self.shortest_path(from_status, to_status)
        actions = []
        for i in range(len(path) - 1):
            actions += self.chronos_event_actions[(path[i], path[i + 1])]
        return actions


def generate_status_graph() -> ClusterStatusGraph:
    status_graph = ClusterStatusGraph()
    status_graph.add_transitions(
        ClusterStatus.creating,
        [
            (
                ClusterStatus.head_node_up,
                [TransitionAction(HEAD_NODE_UP_ACTION, "Head node is up and ready")],
                [ChronosEventAction(MonitoringState.HEAD_NODE_UP.name, "Head node is up and ready")],
            )
        ],
    )
    status_graph.add_transitions(
        ClusterStatus.head_node_up,
        [
            (
                ClusterStatus.jupyter_up,
                [],
                [ChronosEventAction(MonitoringState.JUPYTER_UP.name, "Jupyter is up and ready")],
            ),
            (
                ClusterStatus.head_node_died,
                [TransitionAction(HEAD_NODE_DIED_ACTION, "Head node is down")],
                [],
            ),
        ],
    )

    status_graph.add_transitions(
        ClusterStatus.jupyter_up,
        [
            (
                ClusterStatus.head_node_up,
                [TransitionAction(HEAD_NODE_UP_ACTION, "Jupyter is down")],
                [],
            ),
            (
                ClusterStatus.active,
                [
                    TransitionAction(WORKER_NODES_UP_ACTION, "Worker nodes are up and ready"),
                    TransitionAction(ACTIVE_ACTION, "Cluster is running"),
                ],
                [
                    ChronosEventAction(
                        MonitoringState.WORKER_NODES_UP.name,
                        "Worker nodes are up and ready",
                    ),
                    ChronosEventAction(MonitoringState.CLUSTER_READY.name, "Cluster is running"),
                ],
            ),
            (
                ClusterStatus.head_node_died,
                [TransitionAction(HEAD_NODE_DIED_ACTION, "Head node is down")],
                [],
            ),
        ],
    )

    status_graph.add_transitions(
        ClusterStatus.active,
        [
            (
                ClusterStatus.head_node_died,
                [TransitionAction(HEAD_NODE_DIED_ACTION, "Head node is down")],
                [],
            ),
            (
                ClusterStatus.worker_nodes_died,
                [TransitionAction(WORKER_NODES_DIED_ACTION, "Some Workers nodes are down")],
                [],
            ),
            (
                ClusterStatus.cluster_died,
                [TransitionAction(CLUSTER_DIED_ACTION, "Cluster is down")],
                [],
            ),
            (
                ClusterStatus.worker_nodes_scaled,
                [TransitionAction(WORKER_NODES_SCALED_ACTION, "Worker nodes are scaled")],
                [],
            ),
        ],
    )

    status_graph.add_transitions(
        ClusterStatus.worker_nodes_scaled,
        [
            (
                ClusterStatus.active,
                [TransitionAction(ACTIVE_ACTION, "Worker nodes are scaled down")],
                [],
            )
        ],
    )

    status_graph.add_transitions(
        ClusterStatus.head_node_died,
        [
            (
                ClusterStatus.head_node_up,
                [TransitionAction(HEAD_NODE_UP_ACTION, "Head node started again")],
                [ChronosEventAction(MonitoringState.HEAD_NODE_UP.name, "Head node is up and ready")],
            ),
            (
                ClusterStatus.cluster_died,
                [TransitionAction(CLUSTER_DIED_ACTION, "Cluster is down")],
                [],
            ),
        ],
    )

    status_graph.add_transitions(
        ClusterStatus.worker_nodes_died,
        [
            (
                ClusterStatus.active,
                [TransitionAction(ACTIVE_ACTION, "Worker nodes are back")],
                [
                    ChronosEventAction(
                        MonitoringState.WORKER_NODES_UP.name,
                        "Worker nodes are up and ready",
                    )
                ],
            ),
            (
                ClusterStatus.head_node_died,
                [TransitionAction(HEAD_NODE_DIED_ACTION, "Head node is down")],
                [],
            ),
        ],
    )

    status_graph.add_transitions(
        ClusterStatus.cluster_died,
        [
            (
                ClusterStatus.head_node_up,
                [TransitionAction(HEAD_NODE_UP_ACTION, "Head node is back")],
                [ChronosEventAction(MonitoringState.HEAD_NODE_UP.name, "Head node is back")],
            ),
            (
                ClusterStatus.inactive,
                [TransitionAction(CLUSTER_INACTIVE_ACTION, "Cluster marked inactive from died state")],
                [ChronosEventAction(MonitoringState.CLUSTER_INACTIVE.name, "Cluster Marked Inactive from Died State")],
            ),
        ],
    )

    return status_graph
