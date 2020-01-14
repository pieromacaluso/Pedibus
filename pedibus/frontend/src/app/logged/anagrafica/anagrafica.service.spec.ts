import { TestBed } from '@angular/core/testing';

import { AnagraficaService } from './anagrafica.service';

describe('AnagraficaService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: AnagraficaService = TestBed.get(AnagraficaService);
    expect(service).toBeTruthy();
  });
});
