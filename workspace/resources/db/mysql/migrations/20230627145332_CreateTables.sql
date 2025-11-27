USE main;

CREATE TABLE IF NOT EXISTS `projects` (
                                  `id` int NOT NULL AUTO_INCREMENT,
                                  `user_id` varchar(255) NOT NULL,
                                  `name` varchar(255) NOT NULL,
                                  `cloned_from` varchar(255),
                                  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `codespaces` (
                                   `id` int NOT NULL AUTO_INCREMENT,
                                   `project_id` int NOT NULL,
                                   `name` varchar(255) NOT NULL,
                                   `sync_location` text,
                                   `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   `cluster_id` varchar(255),
                                   `jupyter_link` varchar(255),
                                   `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                   `last_synced_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                   `user_id` varchar(255),
                                   `sync_job_id` varchar(255),
                                   PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `last_selected_codespace` (
                                   `user_id` varchar(255) NOT NULL,
                                   `codespace_id` int NOT NULL,
                                   PRIMARY KEY (`user_id`)
);