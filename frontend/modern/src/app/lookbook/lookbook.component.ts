import { Component, OnInit } from '@angular/core';
import { forkJoin } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { AuraHubApiService } from '../core/services/aurahub-api.service';
import { Boutique, Collection, PublicPiece } from '../core/models/aurahub.models';

@Component({
  selector: 'app-lookbook',
  templateUrl: './lookbook.component.html',
  styleUrls: ['./lookbook.component.scss']
})
export class LookbookComponent implements OnInit {
  collections: Collection[] = [];
  boutiques: Boutique[] = [];
  pieces: PublicPiece[] = [];
  selectedCollection: Collection | null = null;
  loading = true;
  loadingPieces = false;
  error = '';

  constructor(private readonly api: AuraHubApiService) {}

  ngOnInit(): void {
    forkJoin({
      collections: this.api.publicCollections(),
      boutiques: this.api.publicBoutiques()
    }).pipe(finalize(() => this.loading = false)).subscribe({
      next: result => {
        this.collections = result.collections;
        this.boutiques = result.boutiques;
        if (this.collections.length) {
          this.selectCollection(this.collections[0]);
        }
      },
      error: () => this.error = 'The lookbook is temporarily unavailable. Please try again later.'
    });
  }

  selectCollection(collection: Collection): void {
    this.selectedCollection = collection;
    this.pieces = [];
    this.loadingPieces = true;
    this.api.publicPieces(collection.id).pipe(finalize(() => this.loadingPieces = false)).subscribe({
      next: pieces => this.pieces = pieces,
      error: () => this.error = 'This collection could not be loaded. Please try again later.'
    });
  }

  pieceImage(piece: PublicPiece): string | null {
    const image = piece.images.find(url => !!url) || piece.image;
    return image ? this.assetUrl(image) : null;
  }

  assetUrl(path: string): string {
    if (/^https?:\/\//i.test(path)) {
      return path;
    }
    const apiRoot = environment.apiUrl.replace(/\/api\/?$/, '');
    return `${apiRoot}${path.startsWith('/') ? path : `/${path}`}`;
  }
}
