import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';
import { ToastService } from './core/toast.service';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([])],
    }).compileComponents();
  });

  it('exibe as notificações do ToastService', async () => {
    const fixture = TestBed.createComponent(App);
    TestBed.inject(ToastService).sucesso('Depósito realizado');
    await fixture.whenStable();
    const toast = (fixture.nativeElement as HTMLElement).querySelector('.toast');
    expect(toast?.textContent).toContain('Depósito realizado');
  });
});
