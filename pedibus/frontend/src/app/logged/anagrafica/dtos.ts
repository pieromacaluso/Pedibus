import {ChildrenDTO} from '../genitore/dtos';

export interface UserDTO {
  name: string;
  surname: string;
  userId: string;
  roleIdList: string[];
  lineaIdList: string[];
  childIdList: string[];
}

export interface PageUser {
  content: UserDTO[];
}

export interface PageChild {
  content: ChildrenDTO[];
}
