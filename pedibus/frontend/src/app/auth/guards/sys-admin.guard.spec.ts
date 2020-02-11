import { TestBed, async, inject } from '@angular/core/testing';

import { SysAdminGuard } from './sys-admin.guard';

describe('SysAdminGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SysAdminGuard]
    });
  });

  it('should ...', inject([SysAdminGuard], (guard: SysAdminGuard) => {
    expect(guard).toBeTruthy();
  }));
});
