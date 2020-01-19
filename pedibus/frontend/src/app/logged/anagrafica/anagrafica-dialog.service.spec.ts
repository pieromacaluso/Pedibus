import { TestBed } from '@angular/core/testing';

import { AnagraficaDialogService } from './anagrafica-dialog.service';

describe('AnagraficaDialogService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: AnagraficaDialogService = TestBed.get(AnagraficaDialogService);
    expect(service).toBeTruthy();
  });
});
