-- =====================================================================
--  Aura Hub - jeu de données fictif
--  A exécuter APRÈS un premier démarrage de l'application (Hibernate crée
--  les tables et DataInitializer crée les rôles + le compte admin).
--      mysql -u root -p aurahub < sql/aurahub_seed.sql
--  Base vide recommandée (les identifiants des lignes sont fixés ci-dessous).
--  Comptes :  admin@aura.com / Admin123!   (ADMIN)
--             marie@aura.com / Conseil123!  (CONSEILLER)
--             youssef@aura.com / Conseil123! (CONSEILLER)
-- =====================================================================
SET NAMES utf8mb4;
USE aurahub;

-- ---------- Rôles & utilisateurs
INSERT IGNORE INTO roles (libelle) VALUES ('ADMIN'), ('CONSEILLER');

INSERT IGNORE INTO users (email, password, tel, nom, prenom, enabled) VALUES
 ('admin@aura.com',   '$2a$10$G5pY1DzpQFbq2G4VwVUzpuHsIRc1VueaqEvQL0GiLQo/IjHxCY1G2', '+21670000000', 'Aura',   'Admin',   1),
 ('marie@aura.com',   '$2a$10$oD9t3Vxq.Yzvmmyl8BuAZuvBKMGQMKi8PSWx55kf5GfiFza9CmlAm', '+21620111222', 'Bernard', 'Marie',   1),
 ('youssef@aura.com', '$2a$10$oD9t3Vxq.Yzvmmyl8BuAZuvBKMGQMKi8PSWx55kf5GfiFza9CmlAm', '+21650333444', 'Gharbi',  'Youssef', 1);

INSERT IGNORE INTO user_roles (user_id, role_id)
 SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'admin@aura.com'   AND r.libelle = 'ADMIN';
INSERT IGNORE INTO user_roles (user_id, role_id)
 SELECT u.id, r.id FROM users u, roles r WHERE u.email IN ('marie@aura.com','youssef@aura.com') AND r.libelle = 'CONSEILLER';

SET @marie   = (SELECT id FROM users WHERE email = 'marie@aura.com');
SET @youssef = (SELECT id FROM users WHERE email = 'youssef@aura.com');

-- ---------- Collections (1 en vente depuis 10 jours : accès anticipé terminé ; 1 défilé : non commandable)
INSERT INTO collections (id, nom, saison, annee, theme, date_defile, statut, date_mise_en_vente) VALUES
 (1, 'Lumière de Milan',   'AUTOMNE_HIVER', 2026, 'Noir, ivoire et or',     '2026-02-25', 'EN_VENTE', NOW() - INTERVAL 10 DAY),
 (2, 'Été Méditerranée',   'PRINTEMPS_ETE', 2027, 'Lin, soie et bord de mer', '2026-09-27', 'DEFILE',   NULL);

-- ---------- Boutiques
INSERT INTO boutiques (id, nom, ville, pays, adresse, type) VALUES
 (1, 'Maison Aura Milano',   'Milan', 'Italie',  'Via Montenapoleone 12',                 'FLAGSHIP'),
 (2, 'Maison Aura Tunis',    'Tunis', 'Tunisie', 'Rue du Lac Windermere, Les Berges du Lac', 'BOUTIQUE'),
 (3, 'Aura Pop-up Paris',    'Paris', 'France',  'Rue Saint-Honoré 215',                  'POP_UP');

-- ---------- Pièces
INSERT INTO pieces (id, reference, nom, categorie, matiere, couleur, prix, image, edition_limitee, tirage_max, collection_id) VALUES
 (1,  'AH26-SAC-001', 'Sac Aurelia',     'SAC',        'Cuir de veau',  'Noir',        3200.00, '/uploads/d05ec2de-e4e4-40b4-8b95-f58e9b21c34b.jpg',  1, 150, 1),
 (2,  'AH26-SAC-002', 'Pochette Lune',   'SAC',        'Satin',         'Ivoire',      1450.00, '/uploads/ed955d89-3c05-4564-9404-19773f4cf272.jpg',  0, NULL, 1),
 (3,  'AH26-ROB-001', 'Robe Fiorenza',   'ROBE',       'Soie',          'Ivoire',      2800.00, '/uploads/robe-fiorenza.jpeg',  0, NULL, 1),
 (4,  'AH26-ROB-002', 'Robe Notte',      'ROBE',       'Crêpe',         'Noir',        2400.00, '/uploads/robe-notte.jpeg',  0, NULL, 1),
 (5,  'AH26-MAN-001', 'Manteau Duomo',   'MANTEAU',    'Cachemire',     'Camel',       4200.00, '/uploads/manteau-duomo.jpeg',  1, 80,  1),
 (6,  'AH26-SOU-001', 'Escarpin Lucia',  'SOULIER',    'Cuir',          'Noir',         980.00, '/uploads/escarpin-lucia.jpeg',  0, NULL, 1),
 (7,  'AH26-SOU-002', 'Bottine Brera',  'SOULIER',    'Daim',          'Chocolat',    1150.00, '/uploads/bottine-brera.jpeg',  0, NULL, 1),
 (8,  'AH26-ACC-001', 'Foulard Oro',    'ACCESSOIRE', 'Soie',          'Or',           420.00, '/uploads/foulard-oro.jpeg',  0, NULL, 1),
 (9,  'PE27-SAC-001', 'Cabas Marea',     'SAC',        'Toile et cuir', 'Sable',       2100.00, 'https://picsum.photos/seed/aura9/800/1000',  0, NULL, 2),
 (10, 'PE27-ROB-001', 'Robe Sirocco',    'ROBE',       'Lin',           'Blanc cassé', 1900.00, 'https://picsum.photos/seed/aura10/800/1000', 0, NULL, 2);

INSERT INTO piece_images (piece_id, url) VALUES
 (1,'/uploads/d05ec2de-e4e4-40b4-8b95-f58e9b21c34b.jpg'),
 (2,'/uploads/ed955d89-3c05-4564-9404-19773f4cf272.jpg'),  (3,'/uploads/robe-fiorenza.jpeg'),
 (4,'/uploads/robe-notte.jpeg'),  (5,'/uploads/manteau-duomo.jpeg'),
 (6,'/uploads/escarpin-lucia.jpeg'),  (7,'/uploads/bottine-brera.jpeg'),
 (8,'/uploads/foulard-oro.jpeg'),  (9,'https://picsum.photos/seed/aura9/800/1000'),
 (10,'https://picsum.photos/seed/aura10/800/1000');

-- ---------- Stocks (quantités après les ventes de démonstration ci-dessous)
INSERT INTO stocks (piece_id, boutique_id, taille, quantite) VALUES
 (1,1,'U',12),(1,2,'U',3),(1,3,'U',3),
 (2,1,'U',20),(2,2,'U',8),
 (3,1,'38',4),(3,1,'40',5),(3,2,'38',2),(3,2,'40',3),
 (4,1,'38',5),(4,2,'40',4),(4,3,'38',1),
 (5,1,'M',6),(5,2,'M',1),(5,2,'L',1),
 (6,1,'38',6),(6,2,'39',5),(6,3,'38',2),
 (7,1,'40',4),(7,2,'39',3),
 (8,1,'U',30),(8,2,'U',13),(8,3,'U',9),
 (9,1,'U',10),(10,2,'M',5);

-- ---------- Clients privés
INSERT INTO clients_prives (id, nom, tel, email, pays, tailles, preferences, niveau, conseiller_id) VALUES
 (1, 'Salma Trabelsi',  '+21698111222',  'salma.trabelsi@example.com', 'Tunisie', 'Robe 38, chaussures 39',   'Aime le noir et les sacs structurés',  'PRESTIGE',  @marie),
 (2, 'Giulia Rossi',    '+393331112233', 'giulia.rossi@example.com',   'Italie',  'Robe 40, manteau M',       'Palette neutre, cachemire',            'PRIVILEGE', @marie),
 (3, 'Karim Ben Salah', '+21655333444',  'karim.bensalah@example.com', 'Tunisie', 'Manteau L',                'Accessoires en soie',                  'PRIVILEGE', @marie),
 (4, 'Claire Dubois',   '+33612345678',  'claire.dubois@example.com',  'France',  'Robe 38',                  'Couleurs claires, lin',                'PRIVILEGE', @youssef),
 (5, 'Amine Haddad',    '+21622555666',  'amine.haddad@example.com',   'Tunisie', 'Manteau M, chaussures 42', 'Collectionne les éditions limitées',   'PRESTIGE',  @youssef);

-- ---------- Commandes de démonstration (alimentent le tableau de bord)
INSERT INTO commandes (id, date_commande, statut, total, client_id, boutique_id) VALUES
 (1, NOW() - INTERVAL 5 DAY, 'CONFIRMEE', 3620.00, 1, 2),
 (2, NOW() - INTERVAL 3 DAY, 'CONFIRMEE', 4200.00, 1, 2),
 (3, NOW() - INTERVAL 7 DAY, 'LIVREE',    3780.00, 2, 1),
 (4, NOW() - INTERVAL 2 DAY, 'CONFIRMEE', 7400.00, 5, 2),
 (5, NOW() - INTERVAL 1 DAY, 'BROUILLON', 2400.00, 4, 3);

INSERT INTO lignes_commande (commande_id, piece_id, taille, quantite, prix_unitaire, numero_exemplaire) VALUES
 (1, 1, 'U', 1, 3200.00, 1),
 (1, 8, 'U', 1,  420.00, NULL),
 (2, 5, 'M', 1, 4200.00, 1),
 (3, 3, '40', 1, 2800.00, NULL),
 (3, 6, '38', 1,  980.00, NULL),
 (4, 5, 'L', 1, 4200.00, 2),
 (4, 1, 'U', 1, 3200.00, 2),
 (5, 4, '38', 1, 2400.00, NULL);
