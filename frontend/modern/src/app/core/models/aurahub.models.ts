export interface AuthResponse {
  token: string;
  id: number;
  email: string;
  nom: string;
  prenom: string;
  roles: string[];
}

export type CollectionStatus = 'EN_CONCEPTION' | 'DEFILE' | 'EN_VENTE' | 'ARCHIVEE';
export type Season = 'PRINTEMPS_ETE' | 'AUTOMNE_HIVER';
export type BoutiqueType = 'FLAGSHIP' | 'BOUTIQUE' | 'POP_UP';
export type OrderStatus = 'BROUILLON' | 'CONFIRMEE' | 'LIVREE' | 'ANNULEE';
export type ClientLevel = 'PRIVILEGE' | 'PRESTIGE' | 'ELITE';

export interface Collection {
  id: number;
  nom: string;
  saison: Season;
  annee: number;
  theme: string | null;
  dateDefile: string | null;
  statut: CollectionStatus;
  dateMiseEnVente: string | null;
}

export interface Piece {
  id: number;
  reference: string;
  nom: string;
  categorie: string;
  matiere: string | null;
  couleur: string | null;
  prix: number;
  image: string | null;
  images: string[];
  editionLimitee: boolean;
  tirageMax: number | null;
  collectionId: number;
}

export interface PublicPiece {
  id: number;
  reference: string;
  nom: string;
  categorie: string;
  matiere: string | null;
  couleur: string | null;
  image: string | null;
  images: string[];
  editionLimitee: boolean;
  tirageMax: number | null;
}

export interface Boutique {
  id: number;
  nom: string;
  ville: string;
  pays: string;
  adresse: string | null;
  type: BoutiqueType;
}

export interface Stock {
  id: number;
  pieceId: number;
  boutiqueId: number;
  taille: string;
  quantite: number;
  pieceNom: string;
  boutiqueNom: string;
}

export interface Client {
  id: number;
  nom: string;
  tel: string | null;
  email: string | null;
  pays: string | null;
  tailles: string | null;
  preferences: string | null;
  niveau: ClientLevel;
  conseillerId: number;
  conseillerNom: string;
}

export interface ClientHistory {
  client: Client;
  commandes: Order[];
  totalAchats: number;
}

export interface PieceAvailability {
  boutiqueId: number;
  boutique: string;
  ville: string;
  taille: string;
  quantite: number;
}

export interface OrderLine {
  id: number;
  pieceId: number;
  pieceNom: string;
  taille: string;
  quantite: number;
  prixUnitaire: number;
  numeroExemplaire: number | null;
  tirageMax: number | null;
}

export interface Order {
  id: number;
  date: string;
  statut: OrderStatus;
  total: number;
  clientId: number;
  clientNom: string;
  boutiqueId: number;
  boutiqueNom: string;
  lignes: OrderLine[];
}

export interface UserAccount {
  id: number;
  email: string;
  tel: string | null;
  nom: string;
  prenom: string;
  enabled: boolean;
  roles: string[];
}

export interface UserRequest {
  email: string;
  password?: string;
  tel: string | null;
  nom: string;
  prenom: string;
  roles: string[];
  enabled: boolean;
}

export interface StoreAmount {
  libelle: string;
  montant: number;
}

export interface BestSellingPiece {
  pieceId: number;
  nom: string;
  quantiteVendue: number;
}

export interface CollectionTurnover {
  collectionId: number;
  nom: string;
  vendus: number;
  stockRestant: number;
  taux: number;
}

export interface DashboardStats {
  caParBoutique: StoreAmount[];
  caParCategorie: StoreAmount[];
  piecesPlusVendues: BestSellingPiece[];
  ecoulementCollections: CollectionTurnover[];
}

export interface OrderRequest {
  clientId: number;
  boutiqueId: number;
  lignes: Array<{
    pieceId: number;
    taille: string;
    quantite: number;
  }>;
}
