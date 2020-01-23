import { TestBed } from '@angular/core/testing';

import { ApiTurniService } from './api-turni.service';

describe('ApiTurniService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ApiTurniService = TestBed.get(ApiTurniService);
    expect(service).toBeTruthy();
  });
});
