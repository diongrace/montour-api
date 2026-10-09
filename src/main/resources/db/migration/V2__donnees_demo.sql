insert into etablissement (nom, slug, adresse)
values ('Centre de santé Démo', 'centre-demo', 'Cocody, Abidjan');

insert into service_accueil (etablissement_id, nom, prefixe, ouvert, duree_par_defaut_min)
select id, 'Consultation générale', 'C', true, 5 from etablissement where slug = 'centre-demo';
insert into service_accueil (etablissement_id, nom, prefixe, ouvert, duree_par_defaut_min)
select id, 'Pharmacie', 'P', true, 3 from etablissement where slug = 'centre-demo';
insert into service_accueil (etablissement_id, nom, prefixe, ouvert, duree_par_defaut_min)
select id, 'Laboratoire', 'L', false, 8 from etablissement where slug = 'centre-demo';

insert into guichet (service_id, nom, actif)
select id, 'Guichet 1', true from service_accueil where prefixe = 'C';
insert into guichet (service_id, nom, actif)
select id, 'Guichet 2', true from service_accueil where prefixe = 'C';
insert into guichet (service_id, nom, actif)
select id, 'Guichet 3', true from service_accueil where prefixe = 'P';
insert into guichet (service_id, nom, actif)
select id, 'Guichet 4', true from service_accueil where prefixe = 'L';
