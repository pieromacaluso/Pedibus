import { TestBed } from '@angular/core/testing';

import { ApiDispService } from './api-disp.service';

describe('ApiDispService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ApiDispService = TestBed.get(ApiDispService);
    expect(service).toBeTruthy();
  });
});
