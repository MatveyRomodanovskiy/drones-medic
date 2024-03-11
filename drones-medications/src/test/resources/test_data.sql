delete from drones;
delete from medications;
delete from drone_models;
delete from event_logs;
insert into drone_models (model_name, weight) values
('Lightweight', 100),
('Middleweight', 300),
('Cruiserweight', 400),
('Heavyweight', 500);

insert into drones (drone_number, model_name,  battery_capacity, state) 
	values
		('Drone-1', 'Middleweight',  100, 'IDLE'),
		('Drone-2', 'Middleweight',  20, 'IDLE'),
		('Drone-3', 'Middleweight',  100, 'LOADING');
insert into medications (code, name, weight)
	values 
		('MED_1', 'Medication-1', 200),
		('MED_2', 'Medication-2', 350)	;
		