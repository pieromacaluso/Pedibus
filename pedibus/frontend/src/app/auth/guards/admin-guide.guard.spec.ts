import { TestBed, async, inject } from '@angular/core/testing';

import { AdminGuideGuard } from './admin-guide.guard';

describe('AdminGuideGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AdminGuideGuard]
    });
  });

  it('should ...', inject([AdminGuideGuard], (guard: AdminGuideGuard) => {
    expect(guard).toBeTruthy();
  }));
});
