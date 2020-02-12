import {TestBed} from '@angular/core/testing';

import {BambinoService} from './bambino.service';

describe('BambinoService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: BambinoService = TestBed.get(BambinoService);
    expect(service).toBeTruthy();
  });
});
