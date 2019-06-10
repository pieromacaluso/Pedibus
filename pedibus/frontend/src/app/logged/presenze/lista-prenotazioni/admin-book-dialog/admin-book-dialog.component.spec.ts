import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminBookDialogComponent } from './admin-book-dialog.component';

describe('AdminBookDialogComponent', () => {
  let component: AdminBookDialogComponent;
  let fixture: ComponentFixture<AdminBookDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AdminBookDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AdminBookDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
