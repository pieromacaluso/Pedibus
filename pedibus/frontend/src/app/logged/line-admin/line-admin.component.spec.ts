import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LineAdminComponent } from './line-admin.component';

describe('LineAdminComponent', () => {
  let component: LineAdminComponent;
  let fixture: ComponentFixture<LineAdminComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LineAdminComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LineAdminComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
