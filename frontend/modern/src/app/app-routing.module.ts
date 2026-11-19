import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { WorkspaceComponent } from './workspace/workspace.component';
import { AuthGuard } from './core/guards/auth.guard';
import { RoleGuard } from './core/guards/role.guard';
import { LookbookComponent } from './lookbook/lookbook.component';

const routes: Routes = [
  { path: 'lookbook', component: LookbookComponent },
  {
    path: 'team-access',
    component: WorkspaceComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['ADMIN'], redirectTo: '/', section: 'users' }
  },
  {
    path: '',
    pathMatch: 'full',
    component: WorkspaceComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['ADMIN', 'CONSEILLER'] }
  },
  { path: 'auth', loadChildren: () => import('./account/account.module').then(m => m.AccountModule) },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
