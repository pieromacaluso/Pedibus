import { TestBed } from '@angular/core/testing';

import { NotificheService } from './notifiche.service';

describe('NotificheService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: NotificheService = TestBed.get(NotificheService);
    expect(service).toBeTruthy();
  });
});
