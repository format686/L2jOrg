CREATE TABLE IF NOT EXISTS `clan_skills` (
	`clan_id` INT NOT NULL DEFAULT '0',
	`skill_id` SMALLINT UNSIGNED NOT NULL DEFAULT '0',
	`skill_level` TINYINT UNSIGNED NOT NULL DEFAULT '0',
	PRIMARY KEY  (`clan_id`,`skill_id`)
);
