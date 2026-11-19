import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, finalize, forkJoin, of } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuraHubApiService } from '../core/services/aurahub-api.service';
import { AuthenticationService } from '../core/services/auth.service';
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
  Stock,
  UserAccount
} from '../core/models/aurahub.models';

type Section = 'dashboard' | 'collections' | 'pieces' | 'boutiques' | 'stocks' | 'clients' | 'orders' | 'users';
type FormValue = string | number | boolean | null;
type FormRecord = Record<string, FormValue>;
type FieldType = 'text' | 'email' | 'password' | 'number' | 'date' | 'select' | 'checkbox';

interface FormOption {
  label: string;
  value: string | number;
}

interface FormField {
  key: string;
  label: string;
  type: FieldType;
  required?: boolean;
  min?: number;
  max?: number;
  options?: FormOption[];
}

@Component({
  selector: 'app-workspace',
  templateUrl: './workspace.component.html',
  styleUrls: ['./workspace.component.scss']
})
export class WorkspaceComponent implements OnInit {
  readonly sections: Array<{ key: Section; label: string; icon: string }> = [
    { key: 'dashboard', label: 'Overview', icon: '◫' },
    { key: 'collections', label: 'Collections', icon: '◇' },
    { key: 'pieces', label: 'Pieces', icon: '◈' },
    { key: 'boutiques', label: 'Boutiques', icon: '⌂' },
    { key: 'stocks', label: 'Stock', icon: '▤' },
    { key: 'clients', label: 'Private clients', icon: '♙' },
    { key: 'orders', label: 'Orders', icon: '▧' }
  ];

  activeSection: Section = 'dashboard';
  collections: Collection[] = [];
  pieces: Piece[] = [];
  boutiques: Boutique[] = [];
  stocks: Stock[] = [];
  clients: Client[] = [];
  orders: Order[] = [];
  users: UserAccount[] = [];
  stats: DashboardStats | null = null;
  clientHistory: ClientHistory | null = null;
  clientHistoryLoading = false;
  clientHistoryError = '';
  selectedImageFiles: File[] = [];
  availableStock: PieceAvailability[] = [];
  availabilityLoading = false;
  orderPieceSearch = '';
  private availabilityPieceId: number | null = null;
  private clientHistoryRequestId = 0;
  loading = false;
  saving = false;
  editorOpen = false;
  searchText = '';
  error = '';
  notice = '';
  editingId: number | null = null;
  formFields: FormField[] = [];
  formValue: FormRecord = {};

  constructor(
    private readonly api: AuraHubApiService,
    private readonly authenticationService: AuthenticationService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.loadSection(this.router.url.startsWith('/team-access') ? 'users' : 'dashboard');
  }

  get userName(): string {
    const user = this.authenticationService.currentUser();
    return user ? `${user.prenom} ${user.nom}` : 'Team member';
  }

  get userRole(): string {
    return this.isAdmin ? 'Administrator' : 'Client advisor';
  }

  get isAdmin(): boolean {
    return this.authenticationService.currentUser()?.roles.includes('ADMIN') ?? false;
  }

  get pageTitle(): string {
    const section = this.sections.find(item => item.key === this.activeSection);
    if (section) {
      return section.label;
    }
    return this.activeSection === 'users' ? 'Team access' : 'Overview';
  }

  get pageDescription(): string {
    const descriptions: Record<Section, string> = {
      dashboard: 'A clear view of today across Maison Aura.',
      collections: 'Shape the seasons, runway moments and releases.',
      pieces: 'Manage the pieces and editions in each collection.',
      boutiques: 'Keep your Maison Aura locations up to date.',
      stocks: 'Monitor availability by piece, boutique and size.',
      clients: 'Remember the preferences behind every relationship.',
      orders: 'Create and follow private client orders.',
      users: 'Review team access and activate new accounts.'
    };
    return descriptions[this.activeSection];
  }

  get visibleCollections(): Collection[] {
    return this.filterRows(this.collections);
  }

  get visiblePieces(): Piece[] {
    return this.filterRows(this.pieces);
  }

  get visibleBoutiques(): Boutique[] {
    return this.filterRows(this.boutiques);
  }

  get visibleStocks(): Stock[] {
    return this.filterRows(this.stocks);
  }

  get visibleClients(): Client[] {
    return this.filterRows(this.clients);
  }

  get visibleOrders(): Order[] {
    return this.filterRows(this.orders);
  }

  get visibleUsers(): UserAccount[] {
    return this.filterRows(this.users);
  }

  get lowStockCount(): number {
    return this.stocks.filter(stock => stock.quantite <= 3).length;
  }

  get lowStockItems(): Stock[] {
    return this.stocks.filter(stock => stock.quantite <= 3).slice(0, 5);
  }

  get selectedOrderAvailability(): PieceAvailability | null {
    const pieceId = this.number('pieceId');
    const boutiqueId = this.number('boutiqueId');
    const size = this.text('taille');
    return this.availableStock.find(stock =>
      stock.boutiqueId === boutiqueId && stock.taille === size && pieceId === this.availabilityPieceId
    ) ?? null;
  }

  get selectedOrderPiece(): Piece | null {
    return this.pieces.find(piece => piece.id === this.number('pieceId')) ?? null;
  }

  get editingPieceImages(): string[] {
    const piece = this.pieces.find(item => item.id === this.editingId);
    return [...new Set([...(piece?.images ?? []), piece?.image ?? ''].filter(Boolean))];
  }

  pieceImageUrl(path: string): string {
    if (/^https?:\/\//i.test(path)) {
      return path;
    }
    const apiRoot = environment.apiUrl.replace(/\/api\/?$/, '');
    return `${apiRoot}${path.startsWith('/') ? path : `/${path}`}`;
  }

  get recentOrders(): Order[] {
    return this.orders.slice(0, 5);
  }

  get draftOrderCount(): number {
    return this.orders.filter(order => order.statut === 'BROUILLON').length;
  }

  rowCount(): number {
    switch (this.activeSection) {
      case 'collections': return this.visibleCollections.length;
      case 'pieces': return this.visiblePieces.length;
      case 'boutiques': return this.visibleBoutiques.length;
      case 'stocks': return this.visibleStocks.length;
      case 'clients': return this.visibleClients.length;
      case 'orders': return this.visibleOrders.length;
      case 'users': return this.visibleUsers.length;
      default: return 0;
    }
  }

  collectionName(id: number): string {
    return this.collections.find(collection => collection.id === id)?.nom ?? '—';
  }

  loadSection(section: Section, preserveNotice = false): void {
    this.activeSection = section;
    this.searchText = '';
    this.error = '';
    if (!preserveNotice) {
      this.notice = '';
    }
    this.loading = true;

    switch (section) {
      case 'dashboard':
        this.fetch(
          forkJoin({
            collections: this.api.collections(),
            pieces: this.api.pieces(),
            boutiques: this.api.boutiques(),
            stocks: this.api.stocks(),
            clients: this.api.clients(),
            orders: this.api.orders(),
            stats: this.isAdmin ? this.api.stats() : of(null)
          }),
          result => {
            this.collections = result.collections;
            this.pieces = result.pieces;
            this.boutiques = result.boutiques;
            this.stocks = result.stocks;
            this.clients = result.clients;
            this.orders = result.orders;
            this.stats = result.stats;
          }
        );
        break;
      case 'collections':
        this.fetch(this.api.collections(), result => this.collections = result);
        break;
      case 'pieces':
        this.fetch(
          forkJoin({ pieces: this.api.pieces(), collections: this.api.collections() }),
          result => {
            this.pieces = result.pieces;
            this.collections = result.collections;
          }
        );
        break;
      case 'boutiques':
        this.fetch(this.api.boutiques(), result => this.boutiques = result);
        break;
      case 'stocks':
        this.fetch(
          forkJoin({ stocks: this.api.stocks(), pieces: this.api.pieces(), boutiques: this.api.boutiques() }),
          result => {
            this.stocks = result.stocks;
            this.pieces = result.pieces;
            this.boutiques = result.boutiques;
          }
        );
        break;
      case 'clients':
        this.fetch(this.api.clients(), result => this.clients = result);
        break;
      case 'orders':
        this.fetch(
          forkJoin({
            orders: this.api.orders(),
            clients: this.api.clients(),
            boutiques: this.api.boutiques(),
            pieces: this.api.pieces()
          }),
          result => {
            this.orders = result.orders;
            this.clients = result.clients;
            this.boutiques = result.boutiques;
            this.pieces = result.pieces;
          }
        );
        break;
      case 'users':
        this.fetch(this.api.users(), result => this.users = result);
        break;
    }
  }

  navigateToTeamAccess(): void {
    void this.router.navigate(['/team-access']);
  }

  openCreate(): void {
    if (this.activeSection === 'dashboard') {
      return;
    }
    this.editingId = null;
    this.formFields = this.fieldsFor(this.activeSection);
    this.formValue = {};
    this.selectedImageFiles = [];
    this.availableStock = [];
    this.availabilityPieceId = null;
    this.orderPieceSearch = '';
    for (const field of this.formFields) {
      const optionValue = field.options?.[0]?.value;
      this.formValue[field.key] = field.type === 'checkbox'
        ? false
        : field.type === 'number'
          ? field.min ?? 0
          : optionValue ?? '';
    }
    if (this.activeSection === 'orders') {
      this.filterOrderPieces('');
      this.formValue['quantite'] = 1;
      this.onEditorFieldChange('pieceId');
    }
    this.editorOpen = true;
  }

  openEdit(row: object): void {
    const source = row as Record<string, unknown>;
    this.editingId = typeof source['id'] === 'number' ? source['id'] : null;
    this.formFields = this.fieldsFor(this.activeSection);
    this.formValue = {};
    this.selectedImageFiles = [];
    for (const field of this.formFields) {
      const value = source[field.key];
      this.formValue[field.key] = value === null || value === undefined
        ? field.type === 'checkbox' ? false : ''
        : typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean'
          ? value
          : '';
    }
    if (this.activeSection === 'users') {
      const roles = source['roles'];
      this.formValue['role'] = Array.isArray(roles) && typeof roles[0] === 'string' ? roles[0] : 'CONSEILLER';
      this.formValue['password'] = '';
    }
    this.editorOpen = true;
  }

  closeEditor(): void {
    if (!this.saving) {
      this.editorOpen = false;
    }
  }

  saveEditor(): void {
    if (this.saving) {
      return;
    }
    this.error = '';
    if (this.activeSection === 'orders') {
      const availability = this.selectedOrderAvailability;
      const piece = this.pieces.find(item => item.id === this.number('pieceId'));
      if (!availability) {
        this.error = 'Choose a size with available stock in the selected boutique.';
        return;
      }
      if (this.number('quantite') > availability.quantite) {
        this.error = `Only ${availability.quantite} unit(s) are available in this size and boutique.`;
        return;
      }
      if (piece?.editionLimitee && this.number('quantite') !== 1) {
        this.error = 'A limited-edition order must contain exactly one numbered piece.';
        return;
      }
    }

    switch (this.activeSection) {
      case 'collections':
        this.execute(this.api.saveCollection({
          id: this.editingId ?? undefined,
          nom: this.text('nom'),
          saison: this.text('saison') as Collection['saison'],
          annee: this.number('annee'),
          theme: this.text('theme') || null,
          dateDefile: this.text('dateDefile') || null,
          statut: this.text('statut') as Collection['statut']
        }), 'Collection saved.');
        break;
      case 'pieces':
        this.execute(this.api.savePiece({
          id: this.editingId ?? undefined,
          reference: this.text('reference'),
          nom: this.text('nom'),
          categorie: this.text('categorie'),
          matiere: this.text('matiere') || null,
          couleur: this.text('couleur') || null,
          prix: this.number('prix'),
          image: this.text('image') || null,
          editionLimitee: this.boolean('editionLimitee'),
          tirageMax: this.boolean('editionLimitee') ? this.number('tirageMax') : null,
          collectionId: this.number('collectionId')
        }), 'Piece saved.');
        break;
      case 'boutiques':
        this.execute(this.api.saveBoutique({
          id: this.editingId ?? undefined,
          nom: this.text('nom'),
          ville: this.text('ville'),
          pays: this.text('pays'),
          adresse: this.text('adresse') || null,
          type: this.text('type') as Boutique['type']
        }), 'Boutique saved.');
        break;
      case 'stocks':
        this.execute(this.api.saveStock({
          id: this.editingId ?? undefined,
          pieceId: this.number('pieceId'),
          boutiqueId: this.number('boutiqueId'),
          taille: this.text('taille'),
          quantite: this.number('quantite')
        }), 'Stock saved.');
        break;
      case 'clients':
        this.execute(this.api.saveClient({
          id: this.editingId ?? undefined,
          nom: this.text('nom'),
          tel: this.text('tel') || null,
          email: this.text('email') || null,
          pays: this.text('pays') || null,
          tailles: this.text('tailles') || null,
          preferences: this.text('preferences') || null
        }), 'Client saved.');
        break;
      case 'orders':
        this.execute(this.api.createOrder(this.orderRequest()), 'Order draft created.');
        break;
      case 'users':
        this.execute(this.api.saveUser({
          email: this.text('email'),
          password: this.text('password') || undefined,
          tel: this.text('tel') || null,
          nom: this.text('nom'),
          prenom: this.text('prenom'),
          roles: [this.text('role') || 'CONSEILLER'],
          enabled: this.boolean('enabled')
        }, this.editingId ?? undefined), 'Team account saved.');
        break;
      default:
        this.editorOpen = false;
    }
  }

  deleteCollection(collection: Collection): void {
    if (this.confirmDelete(collection.nom)) {
      this.execute(this.api.deleteCollection(collection.id), 'Collection deleted.');
    }
  }

  deletePiece(piece: Piece): void {
    if (this.confirmDelete(piece.nom)) {
      this.execute(this.api.deletePiece(piece.id), 'Piece deleted.');
    }
  }

  deleteBoutique(boutique: Boutique): void {
    if (this.confirmDelete(boutique.nom)) {
      this.execute(this.api.deleteBoutique(boutique.id), 'Boutique deleted.');
    }
  }

  deleteStock(stock: Stock): void {
    if (this.confirmDelete(`${stock.pieceNom} · ${stock.taille}`)) {
      this.execute(this.api.deleteStock(stock.id), 'Stock row deleted.');
    }
  }

  deleteClient(client: Client): void {
    if (this.confirmDelete(client.nom)) {
      this.execute(this.api.deleteClient(client.id), 'Client deleted.');
    }
  }

  deleteOrder(order: Order): void {
    if (this.confirmDelete(`Draft order #${order.id}`)) {
      this.execute(this.api.deleteOrder(order.id), 'Draft order deleted.');
    }
  }

  deleteUser(user: UserAccount): void {
    if (this.confirmDelete(`${user.prenom} ${user.nom}`)) {
      this.execute(this.api.deleteUser(user.id), 'Team account deleted.');
    }
  }

  activateUser(user: UserAccount): void {
    this.execute(this.api.activateUser(user.id), 'Account activated.');
  }

  openClientHistory(client: Client): void {
    const requestId = ++this.clientHistoryRequestId;
    this.clientHistory = null;
    this.clientHistoryError = '';
    this.clientHistoryLoading = true;
    this.api.clientHistory(client.id).pipe(finalize(() => {
      if (requestId === this.clientHistoryRequestId) {
        this.clientHistoryLoading = false;
      }
    })).subscribe({
      next: history => {
        if (requestId === this.clientHistoryRequestId) {
          this.clientHistory = history;
        }
      },
      error: error => {
        if (requestId === this.clientHistoryRequestId) {
          this.clientHistoryError = this.getErrorMessage(error);
        }
      }
    });
  }

  closeClientHistory(): void {
    this.clientHistoryRequestId++;
    this.clientHistoryLoading = false;
    this.clientHistory = null;
    this.clientHistoryError = '';
  }

  onImageFilesSelected(event: Event): void {
    const input = event.target;
    if (input instanceof HTMLInputElement) {
      this.selectedImageFiles = Array.from(input.files ?? []);
    }
  }

  uploadPieceImages(): void {
    if (this.activeSection !== 'pieces' || this.editingId === null || !this.selectedImageFiles.length) {
      return;
    }
    this.execute(this.api.uploadPieceImages(this.editingId, this.selectedImageFiles), 'Piece images uploaded.');
  }

  onEditorFieldChange(key: string): void {
    if (this.activeSection !== 'orders' || (key !== 'pieceId' && key !== 'boutiqueId')) {
      return;
    }

    this.formValue['taille'] = '';
    const sizeField = this.formFields.find(field => field.key === 'taille');
    if (sizeField) {
      sizeField.options = [];
    }

    const pieceId = this.number('pieceId');
    if (!pieceId) {
      this.availableStock = [];
      this.availabilityPieceId = null;
      return;
    }
    if (pieceId === this.availabilityPieceId) {
      this.updateAvailableSizes();
      return;
    }

    this.availableStock = [];
    this.availabilityPieceId = null;
    this.availabilityLoading = true;
    this.api.pieceAvailability(pieceId).pipe(finalize(() => this.availabilityLoading = false)).subscribe({
      next: stock => {
        if (this.number('pieceId') !== pieceId) {
          return;
        }
        this.availableStock = stock;
        this.availabilityPieceId = pieceId;
        this.updateAvailableSizes();
      },
      error: error => this.error = this.getErrorMessage(error)
    });
  }

  filterOrderPieces(query: string): void {
    this.orderPieceSearch = query;
    const pieceField = this.formFields.find(field => field.key === 'pieceId');
    if (!pieceField) {
      return;
    }
    const normalizedQuery = query.trim().toLowerCase();
    pieceField.options = this.pieces
      .filter(piece => !normalizedQuery
        || piece.nom.toLowerCase().includes(normalizedQuery)
        || piece.reference.toLowerCase().includes(normalizedQuery))
      .map(piece => ({ label: `${piece.reference} · ${piece.nom}`, value: piece.id }));

    if (!pieceField.options.some(option => option.value === this.number('pieceId'))) {
      this.formValue['pieceId'] = pieceField.options[0]?.value ?? '';
      this.onEditorFieldChange('pieceId');
    }
  }

  updateOrder(order: Order, action: 'confirmer' | 'annuler' | 'livrer'): void {
    const actionLabel = action === 'confirmer' ? 'confirmed' : action === 'annuler' ? 'cancelled' : 'marked as delivered';
    this.execute(this.api.updateOrderStatus(order.id, action), `Order ${actionLabel}.`);
  }

  logout(): void {
    this.authenticationService.logout();
    void this.router.navigate(['/auth/login']);
  }

  trackById(_index: number, row: { id: number }): number {
    return row.id;
  }

  private fetch<T>(request: Observable<T>, update: (result: T) => void): void {
    request.pipe(finalize(() => this.loading = false)).subscribe({
      next: result => update(result),
      error: error => this.error = this.getErrorMessage(error)
    });
  }

  private execute(request: Observable<unknown>, successMessage: string): void {
    this.saving = true;
    request.pipe(finalize(() => this.saving = false)).subscribe({
      next: () => {
        this.editorOpen = false;
        this.notice = successMessage;
        this.loadSection(this.activeSection, true);
      },
      error: error => this.error = this.getErrorMessage(error)
    });
  }

  private fieldsFor(section: Section): FormField[] {
    switch (section) {
      case 'collections':
        return [
          { key: 'nom', label: 'Collection name', type: 'text', required: true },
          { key: 'saison', label: 'Season', type: 'select', required: true, options: this.options(['PRINTEMPS_ETE', 'AUTOMNE_HIVER']) },
          { key: 'annee', label: 'Year', type: 'number', required: true, min: 2000 },
          { key: 'theme', label: 'Theme', type: 'text' },
          { key: 'dateDefile', label: 'Runway date', type: 'date' },
          { key: 'statut', label: 'Status', type: 'select', required: true, options: this.options(['EN_CONCEPTION', 'DEFILE', 'EN_VENTE', 'ARCHIVEE']) }
        ];
      case 'pieces':
        return [
          { key: 'reference', label: 'Reference', type: 'text', required: true },
          { key: 'nom', label: 'Piece name', type: 'text', required: true },
          { key: 'categorie', label: 'Category', type: 'select', required: true, options: this.options(['SAC', 'ROBE', 'MANTEAU', 'SOULIER', 'ACCESSOIRE']) },
          { key: 'collectionId', label: 'Collection', type: 'select', required: true, options: this.collections.map(item => ({ label: item.nom, value: item.id })) },
          { key: 'matiere', label: 'Material', type: 'text' },
          { key: 'couleur', label: 'Color', type: 'text' },
          { key: 'prix', label: 'Price (€)', type: 'number', required: true, min: 0 },
          { key: 'image', label: 'Image URL', type: 'text' },
          { key: 'editionLimitee', label: 'Limited edition', type: 'checkbox' },
          { key: 'tirageMax', label: 'Edition size', type: 'number', min: 1 }
        ];
      case 'boutiques':
        return [
          { key: 'nom', label: 'Boutique name', type: 'text', required: true },
          { key: 'ville', label: 'City', type: 'text', required: true },
          { key: 'pays', label: 'Country', type: 'text', required: true },
          { key: 'adresse', label: 'Address', type: 'text' },
          { key: 'type', label: 'Type', type: 'select', required: true, options: this.options(['FLAGSHIP', 'BOUTIQUE', 'POP_UP']) }
        ];
      case 'stocks':
        return [
          { key: 'pieceId', label: 'Piece', type: 'select', required: true, options: this.pieces.map(item => ({ label: `${item.reference} · ${item.nom}`, value: item.id })) },
          { key: 'boutiqueId', label: 'Boutique', type: 'select', required: true, options: this.boutiques.map(item => ({ label: item.nom, value: item.id })) },
          { key: 'taille', label: 'Size', type: 'text', required: true },
          { key: 'quantite', label: 'Quantity', type: 'number', required: true, min: 0 }
        ];
      case 'clients':
        return [
          { key: 'nom', label: 'Full name', type: 'text', required: true },
          { key: 'tel', label: 'Phone', type: 'text' },
          { key: 'email', label: 'Email', type: 'email' },
          { key: 'pays', label: 'Country', type: 'text' },
          { key: 'tailles', label: 'Sizes', type: 'text' },
          { key: 'preferences', label: 'Preferences and notes', type: 'text' }
        ];
      case 'orders':
        return [
          { key: 'clientId', label: 'Private client', type: 'select', required: true, options: this.clients.map(item => ({ label: item.nom, value: item.id })) },
          { key: 'boutiqueId', label: 'Boutique', type: 'select', required: true, options: this.boutiques.map(item => ({ label: item.nom, value: item.id })) },
          { key: 'pieceId', label: 'Piece', type: 'select', required: true, options: this.pieces.map(item => ({ label: `${item.reference} · ${item.nom}`, value: item.id })) },
          { key: 'taille', label: 'Available size', type: 'select', required: true, options: [] },
          { key: 'quantite', label: 'Quantity', type: 'number', required: true, min: 1 }
        ];
      case 'users':
        return [
          { key: 'prenom', label: 'First name', type: 'text', required: true },
          { key: 'nom', label: 'Last name', type: 'text', required: true },
          { key: 'email', label: 'Email', type: 'email', required: true },
          { key: 'tel', label: 'Phone', type: 'text' },
          { key: 'password', label: this.editingId ? 'New password (optional)' : 'Temporary password', type: 'password', required: !this.editingId },
          { key: 'role', label: 'Role', type: 'select', required: true, options: this.options(['CONSEILLER', 'ADMIN']) },
          { key: 'enabled', label: 'Account active', type: 'checkbox' }
        ];
      default:
        return [];
    }
  }

  private orderRequest(): OrderRequest {
    return {
      clientId: this.number('clientId'),
      boutiqueId: this.number('boutiqueId'),
      lignes: [{
        pieceId: this.number('pieceId'),
        taille: this.text('taille'),
        quantite: this.number('quantite')
      }]
    };
  }

  private updateAvailableSizes(): void {
    const sizeField = this.formFields.find(field => field.key === 'taille');
    if (!sizeField) {
      return;
    }
    sizeField.options = this.availableStock
      .filter(stock => stock.boutiqueId === this.number('boutiqueId') && stock.quantite > 0)
      .map(stock => ({ label: `${stock.taille} · ${stock.quantite} available`, value: stock.taille }));
  }

  private options(values: string[]): FormOption[] {
    return values.map(value => ({
      value,
      label: value.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, char => char.toUpperCase())
    }));
  }

  private text(key: string): string {
    const value = this.formValue[key];
    return typeof value === 'string' ? value.trim() : value === null || value === undefined ? '' : String(value);
  }

  private number(key: string): number {
    const value = this.formValue[key];
    return typeof value === 'number' ? value : Number(value ?? 0);
  }

  private boolean(key: string): boolean {
    return this.formValue[key] === true;
  }

  private filterRows<T extends object>(rows: T[]): T[] {
    const query = this.searchText.trim().toLowerCase();
    return query ? rows.filter(row => JSON.stringify(row).toLowerCase().includes(query)) : rows;
  }

  private confirmDelete(name: string): boolean {
    return window.confirm(`Delete ${name}? This action cannot be undone.`);
  }

  private getErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (typeof error.error?.message === 'string') {
        return error.error.message;
      }
      if (error.status === 401) {
        return 'Your session expired. Please sign in again.';
      }
      if (error.status === 403) {
        return 'You do not have permission to perform this action.';
      }
      if (error.status === 0) {
        return `The Aura Hub API is unavailable. Check that the backend is running at ${environment.apiUrl}.`;
      }
    }
    return 'The request could not be completed. Please try again.';
  }
}
