import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Boutique,
  Client,
  ClientHistory,
  Collection,
  DashboardStats,
  Order,
  OrderRequest,
  Piece,
  PieceAvailability,
  PublicPiece,
  Stock,
  UserAccount,
  UserRequest
} from '../models/aurahub.models';

@Injectable({ providedIn: 'root' })
export class AuraHubApiService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

  publicCollections(): Observable<Collection[]> {
    return this.http.get<Collection[]>(`${this.apiUrl}/public/collections/en-cours`);
  }

  publicPieces(collectionId: number): Observable<PublicPiece[]> {
    return this.http.get<PublicPiece[]>(`${this.apiUrl}/public/collections/${collectionId}/pieces`);
  }

  publicBoutiques(): Observable<Boutique[]> {
    return this.http.get<Boutique[]>(`${this.apiUrl}/public/boutiques`);
  }

  collections(): Observable<Collection[]> {
    return this.http.get<Collection[]>(`${this.apiUrl}/collections`);
  }

  saveCollection(collection: Partial<Collection>): Observable<Collection> {
    return collection.id
      ? this.http.put<Collection>(`${this.apiUrl}/collections/${collection.id}`, collection)
      : this.http.post<Collection>(`${this.apiUrl}/collections`, collection);
  }

  deleteCollection(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/collections/${id}`);
  }

  pieces(): Observable<Piece[]> {
    return this.http.get<Piece[]>(`${this.apiUrl}/pieces`);
  }

  savePiece(piece: Partial<Piece>): Observable<Piece> {
    return piece.id
      ? this.http.put<Piece>(`${this.apiUrl}/pieces/${piece.id}`, piece)
      : this.http.post<Piece>(`${this.apiUrl}/pieces`, piece);
  }

  deletePiece(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/pieces/${id}`);
  }

  pieceAvailability(id: number): Observable<PieceAvailability[]> {
    return this.http.get<PieceAvailability[]>(`${this.apiUrl}/pieces/${id}/disponibilite`);
  }

  uploadPieceImages(id: number, files: File[]): Observable<Piece> {
    const body = new FormData();
    files.forEach(file => body.append('files', file));
    return this.http.post<Piece>(`${this.apiUrl}/pieces/${id}/images`, body);
  }

  boutiques(): Observable<Boutique[]> {
    return this.http.get<Boutique[]>(`${this.apiUrl}/boutiques`);
  }

  saveBoutique(boutique: Partial<Boutique>): Observable<Boutique> {
    return boutique.id
      ? this.http.put<Boutique>(`${this.apiUrl}/boutiques/${boutique.id}`, boutique)
      : this.http.post<Boutique>(`${this.apiUrl}/boutiques`, boutique);
  }

  deleteBoutique(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/boutiques/${id}`);
  }

  stocks(): Observable<Stock[]> {
    return this.http.get<Stock[]>(`${this.apiUrl}/stocks`);
  }

  saveStock(stock: Partial<Stock>): Observable<Stock> {
    return stock.id
      ? this.http.put<Stock>(`${this.apiUrl}/stocks/${stock.id}`, stock)
      : this.http.post<Stock>(`${this.apiUrl}/stocks`, stock);
  }

  deleteStock(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/stocks/${id}`);
  }

  clients(): Observable<Client[]> {
    return this.http.get<Client[]>(`${this.apiUrl}/clients`);
  }

  saveClient(client: Partial<Client>): Observable<Client> {
    return client.id
      ? this.http.put<Client>(`${this.apiUrl}/clients/${client.id}`, client)
      : this.http.post<Client>(`${this.apiUrl}/clients`, client);
  }

  deleteClient(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/clients/${id}`);
  }

  clientHistory(id: number): Observable<ClientHistory> {
    return this.http.get<ClientHistory>(`${this.apiUrl}/clients/${id}/historique`);
  }

  orders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.apiUrl}/commandes`);
  }

  createOrder(order: OrderRequest): Observable<Order> {
    return this.http.post<Order>(`${this.apiUrl}/commandes`, order);
  }

  updateOrderStatus(id: number, status: 'confirmer' | 'annuler' | 'livrer'): Observable<Order> {
    return this.http.put<Order>(`${this.apiUrl}/commandes/${id}/${status}`, {});
  }

  deleteOrder(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/commandes/${id}`);
  }

  stats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.apiUrl}/stats`);
  }

  users(): Observable<UserAccount[]> {
    return this.http.get<UserAccount[]>(`${this.apiUrl}/utilisateurs`);
  }

  saveUser(user: UserRequest, id?: number): Observable<UserAccount> {
    return id
      ? this.http.put<UserAccount>(`${this.apiUrl}/utilisateurs/${id}`, user)
      : this.http.post<UserAccount>(`${this.apiUrl}/utilisateurs`, user);
  }

  activateUser(id: number): Observable<UserAccount> {
    return this.http.put<UserAccount>(`${this.apiUrl}/utilisateurs/${id}/activer`, {});
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/utilisateurs/${id}`);
  }
}
