import { TestBed, async, inject } from '@angular/core/testing';

import { GuideGuard } from './guide.guard';

describe('GuideGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [GuideGuard]
    });
  });

  it('should ...', inject([GuideGuard], (guard: GuideGuard) => {
    expect(guard).toBeTruthy();
  }));
});
